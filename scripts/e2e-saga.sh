#!/usr/bin/env bash
#
# E2E da saga: sobe infra + os 3 serviços, cadastra um produto, cria um pedido e acompanha
# order.created -> inventory.reserved -> payment.approved -> pedido PAYMENT_APPROVED.
#
# Pré-requisitos: Docker, Java 17+, os jars buildados (./mvnw -q -DskipTests install).
# Uso: ./scripts/e2e-saga.sh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$(mktemp -d)"
PIDS=()

cleanup() {
  echo "--- encerrando serviços ---"
  for pid in "${PIDS[@]:-}"; do kill "$pid" 2>/dev/null || true; done
  "$ROOT/scripts/stop-local.sh" || true
  echo "logs em $LOG_DIR"
}
trap cleanup EXIT

# Extrai o valor de um campo string de um JSON simples (sem jq/python).
json_field() {
  grep -oE "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed -E "s/.*:[[:space:]]*\"([^\"]*)\"/\1/"
}

new_uuid() {
  if command -v uuidgen >/dev/null 2>&1; then uuidgen
  elif [ -r /proc/sys/kernel/random/uuid ]; then cat /proc/sys/kernel/random/uuid
  else powershell.exe -NoProfile -Command "[guid]::NewGuid().ToString()" | tr -d '\r'
  fi
}

echo "=== 1/5 infra ==="
"$ROOT/scripts/start-local.sh"

start_service() {
  local name="$1" module="$2" port="$3" db="$4"
  local jar
  jar=$(ls "$ROOT/services/$module/target/"*-SNAPSHOT.jar | head -1)
  echo "iniciando $name (porta $port)"
  DB_URL="jdbc:postgresql://localhost:5432/$db" \
  KAFKA_BOOTSTRAP_SERVERS="localhost:9092" \
  java -jar "$jar" --server.port="$port" > "$LOG_DIR/$name.log" 2>&1 &
  PIDS+=($!)
}

echo "=== 2/5 serviços ==="
start_service inventory InventoryService 8083 inventoryservice
start_service payment   PaymentService   8082 paymentservice
start_service order     OrderService     8081 orderservice

wait_up() { local url="$1"; for _ in $(seq 1 60); do curl -sf "$url/actuator/health" >/dev/null 2>&1 && return 0; sleep 2; done; echo "timeout: $url"; return 1; }
wait_up http://localhost:8083
wait_up http://localhost:8082
wait_up http://localhost:8081

echo "=== 3/5 produto ==="
PRODUCT_ID=$(curl -sf -X POST http://localhost:8083/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-E2E","name":"Notebook","description":"Notebook","price":1500,"initialStock":10}' \
  | json_field id)
echo "productId=$PRODUCT_ID"

echo "=== 4/5 pedido ==="
ORDER_ID=$(curl -sf -X POST http://localhost:8081/api/v1/orders \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: e2e-1' \
  -d "{\"customer\":{\"customerId\":\"$(new_uuid)\",\"name\":\"Ana\",\"email\":\"ana@example.com\",\"phone\":\"11999999999\"},\"shippingAddress\":{\"street\":\"Rua A\",\"number\":\"10\",\"city\":\"Sao Paulo\",\"zipCode\":\"01000-000\",\"country\":\"BR\"},\"payment\":{\"paymentMethod\":\"CREDIT_CARD\",\"installments\":1,\"amount\":3000},\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":2,\"unitPrice\":1500}]}" \
  | json_field orderId)
echo "orderId=$ORDER_ID"

echo "=== 5/5 acompanhando a saga ==="
STATUS=PENDING_PAYMENT
for _ in $(seq 1 40); do
  STATUS=$(curl -sf "http://localhost:8081/api/v1/orders/$ORDER_ID" | json_field status)
  echo "status=$STATUS"
  [ "$STATUS" != "PENDING_PAYMENT" ] && break
  sleep 2
done

if [ "$STATUS" = "PAYMENT_APPROVED" ]; then
  echo "E2E OK: pedido aprovado pela saga."
  exit 0
fi
echo "E2E FALHOU: status final = $STATUS (esperado PAYMENT_APPROVED). Veja $LOG_DIR/*.log"
exit 1
