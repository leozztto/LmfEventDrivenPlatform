#!/usr/bin/env bash
#
# E2E de borda: sobe AuthService + GatewayService (+ OrderService e infra) nos containers e valida:
#
#   1. register/login via o Gateway (rota /api/v1/auth/** é pública)
#   2. rota downstream (/api/v1/orders/**) COM Bearer  -> repassada ao serviço (status != 401)
#   3. a mesma rota SEM Bearer                          -> 401 na borda (não chega ao downstream)
#   4. rota admin (/api/v1/audit-events) com token não-admin -> 403 na borda
#   5. JWKS do Auth acessível (é o que o Gateway usa para validar o token)
#
# Pré-requisitos: Docker Desktop em execução. Nada de Java/Maven no host.
# Uso:
#   ./scripts/e2e-auth-gateway.sh              # sobe, testa e derruba tudo (down -v) no fim
#   E2E_KEEP=1 ./scripts/e2e-auth-gateway.sh   # mantém o stack de pé no fim
#   E2E_NO_BUILD=1 ./scripts/e2e-auth-gateway.sh   # pula o --build
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE=(docker compose -f "$ROOT/infrastructure/docker/docker-compose.yml")
RUN_ID="$(date +%s)"

GATEWAY_URL="http://localhost:8088"
AUTH_URL="http://localhost:8087"

SERVICES=(postgres db-init zookeeper kafka auth-service gateway-service order-service inventory-service fraud-service audit-service)

cleanup() {
  local ec=$?
  [ $ec -ne 0 ] && {
    echo
    echo "--- FALHOU (exit $ec). Últimas linhas de auth/gateway: ---"
    "${COMPOSE[@]}" logs --tail=30 auth-service gateway-service || true
  }
  if [ "${E2E_KEEP:-0}" = "1" ]; then
    echo "E2E_KEEP=1 — stack mantido de pé. Derrube com: ${COMPOSE[*]} down -v"
  else
    echo "--- derrubando o stack (E2E_KEEP=1 para manter) ---"
    "${COMPOSE[@]}" down -v || true
  fi
}
trap cleanup EXIT

fail() { echo "  ✗ $1"; exit 1; }
ok()   { echo "  ✓ $1"; }

json_field() { grep -oE "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed -E "s/.*:[[:space:]]*\"([^\"]*)\"/\1/" || true; }

http_code() { curl -s -o /dev/null -w '%{http_code}' "$@"; }

wait_health() {
  local name="$1" url="$2"
  for _ in $(seq 1 90); do
    curl -sf "$url/actuator/health" >/dev/null 2>&1 && { echo "  $name OK"; return 0; }
    sleep 2
  done
  fail "timeout esperando $name ($url/actuator/health)"
}

echo "=== 1/5 build + up dos containers ==="
if [ "${E2E_NO_BUILD:-0}" = "1" ]; then "${COMPOSE[@]}" up -d "${SERVICES[@]}"; else "${COMPOSE[@]}" up -d --build "${SERVICES[@]}"; fi

echo "=== 2/5 aguardando Auth e Gateway ==="
wait_health auth    "$AUTH_URL"
wait_health gateway "$GATEWAY_URL"

echo "=== 3/5 register + login via Gateway ==="
USERNAME="e2e-$RUN_ID"
REG_CODE=$(http_code -X POST "$GATEWAY_URL/api/v1/auth/register" -H 'Content-Type: application/json' \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$USERNAME@example.com\",\"password\":\"s3cret123\"}")
[ "$REG_CODE" = "201" ] || fail "register esperado 201, veio $REG_CODE"
ok "usuário registrado ($USERNAME)"

TOKEN=$(curl -s -X POST "$GATEWAY_URL/api/v1/auth/login" -H 'Content-Type: application/json' \
  -d "{\"usernameOrEmail\":\"$USERNAME\",\"password\":\"s3cret123\"}" | json_field accessToken)
[ -n "$TOKEN" ] || fail "login não devolveu accessToken"
ok "login ok, token obtido"

echo "=== 4/5 JWKS do Auth ==="
JWKS_CODE=$(http_code "$AUTH_URL/oauth2/jwks")
[ "$JWKS_CODE" = "200" ] || fail "JWKS esperado 200, veio $JWKS_CODE"
ok "JWKS acessível"

echo "=== 5/5 roteamento + autorização na borda ==="
NO_TOKEN=$(http_code "$GATEWAY_URL/api/v1/products")
[ "$NO_TOKEN" = "401" ] || fail "rota sem token esperado 401, veio $NO_TOKEN"
ok "rota downstream sem token -> 401 na borda"

# /api/v1/products (InventoryService) responde 200 com a lista — prova que o token foi aceito
# e a requisição chegou ao serviço.
WITH_TOKEN=$(http_code -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/v1/products")
[ "$WITH_TOKEN" = "200" ] || fail "rota com token esperado 200 (repassada ao InventoryService), veio $WITH_TOKEN"
ok "rota downstream com token -> repassada ao serviço (HTTP 200)"

ADMIN_ROUTE=$(http_code -H "Authorization: Bearer $TOKEN" "$GATEWAY_URL/api/v1/audit-events?aggregateId=$USERNAME")
[ "$ADMIN_ROUTE" = "403" ] || fail "rota admin com token não-admin esperado 403, veio $ADMIN_ROUTE"
ok "rota admin com token não-admin -> 403 na borda"

DOCS=$(http_code "$GATEWAY_URL/v3/api-docs/swagger-config")
[ "$DOCS" = "200" ] || fail "swagger-config do Gateway esperado 200 (público), veio $DOCS"
ok "Swagger UI agregado acessível em $GATEWAY_URL/swagger-ui.html"

echo
echo "E2E OK — AuthService emitindo JWT e GatewayService validando/roteando na borda."
