#!/usr/bin/env bash
#
# E2E da saga rodando 100% nos containers do Docker Desktop (Postgres + Kafka + os 4 serviços).
# Sobe tudo com `docker compose up -d --build`, exercita os dois caminhos da coreografia e valida:
#
#   1. Aprovado:  order.created -> inventory.reserved -> payment.approved
#                 -> pedido PAYMENT_APPROVED, estoque debitado, reserva CONFIRMED
#   2. Recusado:  order.created -> inventory.reserved -> payment.failed
#                 -> pedido PAYMENT_REJECTED, reserva RELEASED, estoque de volta ao original
#      (o FakePaymentGateway recusa quando amount > 10000)
#
# Também confere o fan-out (o NotificationService registra uma notificação por evento) e que
# nenhuma mensagem parou em tópico .dlt.
#
# Pré-requisitos: Docker Desktop em execução. Nada de Java/Maven no host — o build é na imagem.
# Uso:
#   ./scripts/e2e-docker.sh              # sobe, testa e derruba tudo (down -v) no fim
#   E2E_KEEP=1 ./scripts/e2e-docker.sh   # deixa o stack de pé no fim para inspeção
#   E2E_NO_BUILD=1 ./scripts/e2e-docker.sh   # pula o --build (usa as imagens já construídas)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
COMPOSE=(docker compose -f "$ROOT/infrastructure/docker/docker-compose.yml")
RUN_ID="$(date +%s)"

ORDER_URL="http://localhost:8081"
INVENTORY_URL="http://localhost:8083"
NOTIFICATION_URL="http://localhost:8084"

cleanup() {
  local ec=$?
  [ $ec -ne 0 ] && {
    echo
    echo "--- FALHOU (exit $ec). Últimas linhas dos serviços: ---"
    "${COMPOSE[@]}" logs --tail=25 order-service inventory-service payment-service notification-service || true
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

# Extrai um campo de um JSON simples. Nunca falha (compatível com `set -o pipefail`).
json_field()  { grep -oE "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed -E "s/.*:[[:space:]]*\"([^\"]*)\"/\1/" || true; }
json_number() { grep -oE "\"$1\"[[:space:]]*:[[:space:]]*-?[0-9]+(\.[0-9]+)?" | head -1 | sed -E "s/.*:[[:space:]]*(-?[0-9.]+)/\1/" || true; }

new_uuid() {
  if command -v uuidgen >/dev/null 2>&1; then uuidgen
  elif [ -r /proc/sys/kernel/random/uuid ]; then cat /proc/sys/kernel/random/uuid
  else powershell.exe -NoProfile -Command "[guid]::NewGuid().ToString()" | tr -d '\r'
  fi
}

# POST com corpo JSON. Ecoa o corpo da resposta; aborta com mensagem clara se o status não for 2xx.
api_post() {
  local url="$1" body="$2" resp code
  resp=$(curl -s -w $'\n%{http_code}' -X POST "$url" -H 'Content-Type: application/json' -d "$body")
  code=${resp##*$'\n'}
  resp=${resp%$'\n'*}
  case "$code" in 2*) echo "$resp" ;; *) echo "  ✗ POST $url -> HTTP $code: $resp" >&2; exit 1 ;; esac
}

api_get() { curl -s "$1"; }

wait_health() {
  local name="$1" url="$2"
  for _ in $(seq 1 90); do
    curl -sf "$url/actuator/health" >/dev/null 2>&1 && { echo "  $name OK"; return 0; }
    sleep 2
  done
  fail "timeout esperando $name ($url/actuator/health)"
}

order_status() { api_get "$ORDER_URL/api/v1/orders/$1" | json_field status; }

create_product() {
  local sku="$1" name="$2" price="$3" stock="$4"
  api_post "$INVENTORY_URL/api/v1/products" \
    "{\"sku\":\"$sku\",\"name\":\"$name\",\"description\":\"$name\",\"price\":$price,\"initialStock\":$stock}" \
    | json_field id
}

create_order() {
  local product_id="$1" qty="$2" unit_price="$3" amount="$4" idem="$5" name="$6" email="$7"
  curl -s -X POST "$ORDER_URL/api/v1/orders" \
    -H 'Content-Type: application/json' -H "Idempotency-Key: $idem" \
    -d "{\"customer\":{\"customerId\":\"$(new_uuid)\",\"name\":\"$name\",\"email\":\"$email\",\"phone\":\"11999999999\"},\"shippingAddress\":{\"street\":\"Rua A\",\"number\":\"10\",\"city\":\"Sao Paulo\",\"zipCode\":\"01000-000\",\"country\":\"BR\"},\"payment\":{\"paymentMethod\":\"CREDIT_CARD\",\"installments\":1,\"amount\":$amount},\"items\":[{\"productId\":\"$product_id\",\"quantity\":$qty,\"unitPrice\":$unit_price}]}" \
    | json_field orderId
}

# Aguarda o pedido sair de PENDING_PAYMENT (outbox com poll de 30s -> pode levar ~1min).
await_saga() {
  local order_id="$1" status=""
  for _ in $(seq 1 80); do
    status=$(order_status "$order_id")
    [ -n "$status" ] && [ "$status" != "PENDING_PAYMENT" ] && break
    sleep 3
  done
  echo "${status:-PENDING_PAYMENT}"
}

# Espera (com retry) a notificação do pedido aparecer nos logs. Match de substring puro em bash.
await_notification() {
  local order_id="$1" logs
  for _ in $(seq 1 20); do
    logs=$("${COMPOSE[@]}" logs notification-service 2>&1 || true)
    case "$logs" in *"$order_id"*) return 0 ;; esac
    sleep 3
  done
  return 1
}

echo "=== 1/6 build + up dos containers ==="
if [ "${E2E_NO_BUILD:-0}" = "1" ]; then "${COMPOSE[@]}" up -d; else "${COMPOSE[@]}" up -d --build; fi

echo "=== 2/6 aguardando os serviços ficarem prontos ==="
wait_health order        "$ORDER_URL"
wait_health inventory    "$INVENTORY_URL"
wait_health payment      "http://localhost:8082"
wait_health notification "$NOTIFICATION_URL"

echo "=== 3/6 caminho APROVADO ==="
APPROVED_PID=$(create_product "SKU-E2E-OK-$RUN_ID" "Notebook" 1500 10)
[ -n "$APPROVED_PID" ] || fail "não obtive o id do produto"
echo "  produto=$APPROVED_PID (estoque inicial 10)"
APPROVED_OID=$(create_order "$APPROVED_PID" 2 1500 3000 "e2e-ok-$RUN_ID" "Ana" "ana@example.com")
[ -n "$APPROVED_OID" ] || fail "não obtive o orderId"
echo "  pedido=$APPROVED_OID"
APPROVED_STATUS=$(await_saga "$APPROVED_OID")
echo "  status final=$APPROVED_STATUS"
[ "$APPROVED_STATUS" = "PAYMENT_APPROVED" ] || fail "esperado PAYMENT_APPROVED, veio $APPROVED_STATUS"
ok "pedido aprovado pela saga"
APPROVED_PRODUCT=$(api_get "$INVENTORY_URL/api/v1/products/$APPROVED_PID")
AV=$(printf '%s' "$APPROVED_PRODUCT" | json_number availableQuantity)
RS=$(printf '%s' "$APPROVED_PRODUCT" | json_number reservedQuantity)
[ "$AV" = "8" ] || fail "estoque disponível=$AV (esperado 8 após debitar 2)"
[ "$RS" = "0" ] || fail "estoque reservado=$RS (esperado 0 após confirmar)"
ok "estoque debitado: available=8 reserved=0"

echo "=== 4/6 caminho RECUSADO + compensação ==="
DECLINED_PID=$(create_product "SKU-E2E-DECLINE-$RUN_ID" "Servidor" 10000 10)
[ -n "$DECLINED_PID" ] || fail "não obtive o id do produto"
echo "  produto=$DECLINED_PID (estoque inicial 10)"
DECLINED_OID=$(create_order "$DECLINED_PID" 2 10000 20000 "e2e-decline-$RUN_ID" "Bruno" "bruno@example.com")
[ -n "$DECLINED_OID" ] || fail "não obtive o orderId"
echo "  pedido=$DECLINED_OID (amount=20000, acima do limite do gateway)"
DECLINED_STATUS=$(await_saga "$DECLINED_OID")
sleep 6   # dá tempo da compensação de estoque acontecer após o PAYMENT_REJECTED
echo "  status final=$DECLINED_STATUS"
[ "$DECLINED_STATUS" = "PAYMENT_REJECTED" ] || fail "esperado PAYMENT_REJECTED, veio $DECLINED_STATUS"
ok "pedido recusado pela saga"
DECLINED_PRODUCT=$(api_get "$INVENTORY_URL/api/v1/products/$DECLINED_PID")
AV=$(printf '%s' "$DECLINED_PRODUCT" | json_number availableQuantity)
RS=$(printf '%s' "$DECLINED_PRODUCT" | json_number reservedQuantity)
[ "$AV" = "10" ] || fail "estoque disponível=$AV (esperado 10 — reserva não liberada)"
[ "$RS" = "0" ]  || fail "estoque reservado=$RS (esperado 0)"
ok "reserva compensada: available=10 reserved=0"

echo "=== 5/6 fan-out do NotificationService ==="
await_notification "$APPROVED_OID" || fail "NotificationService não registrou nada para o pedido aprovado"
await_notification "$DECLINED_OID" || fail "NotificationService não registrou nada para o pedido recusado"
ok "notificações emitidas para os dois pedidos"

echo "=== 6/6 checando DLTs (devem estar vazias) ==="
DLT_MSGS=$("${COMPOSE[@]}" exec -T kafka bash -c '
  total=0
  for t in $(kafka-topics --bootstrap-server localhost:9092 --list | grep "\.dlt$"); do
    n=$(kafka-run-class kafka.tools.GetOffsetShell --broker-list localhost:9092 --topic "$t" 2>/dev/null \
        | awk -F: "{s+=\$3} END{print s+0}")
    total=$((total + n))
  done
  echo "$total"
' | tr -d '\r')
[ "${DLT_MSGS:-0}" = "0" ] || fail "há $DLT_MSGS mensagem(ns) em tópicos .dlt"
ok "nenhuma mensagem em DLT"

echo
echo "E2E OK — saga funcionando nos containers do Docker Desktop (aprovação + recusa/compensação + fan-out)."
