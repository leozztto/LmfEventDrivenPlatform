#!/usr/bin/env bash
#
# E2E do caminho de pagamento RECUSADO + compensação de estoque.
#   order.created -> inventory.reserved -> payment.failed
#     -> Order: pedido PAYMENT_REJECTED
#     -> Inventory: reserva RELEASED, estoque disponível volta ao original
#
# O FakePaymentGateway recusa quando amount > 10000.
#
# Pré-requisitos: Docker, Java 17+, jars buildados (./mvnw -q -DskipTests install).
# Uso: ./scripts/e2e-saga-declined.sh
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

json_field() {
  grep -oE "\"$1\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" | head -1 | sed -E "s/.*:[[:space:]]*\"([^\"]*)\"/\1/"
}
json_number() {
  grep -oE "\"$1\"[[:space:]]*:[[:space:]]*-?[0-9]+" | head -1 | sed -E "s/.*:[[:space:]]*(-?[0-9]+)/\1/"
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

echo "=== 3/5 produto (estoque inicial 10) ==="
PRODUCT_JSON=$(curl -sf -X POST http://localhost:8083/api/v1/products \
  -H 'Content-Type: application/json' \
  -d '{"sku":"SKU-DECLINE","name":"Servidor","description":"Servidor","price":10000,"initialStock":10}')
PRODUCT_ID=$(echo "$PRODUCT_JSON" | json_field id)
echo "productId=$PRODUCT_ID  available=$(echo "$PRODUCT_JSON" | json_number availableQuantity)"

echo "=== 4/5 pedido com amount=20000 (> limite do gateway) ==="
ORDER_ID=$(curl -sf -X POST http://localhost:8081/api/v1/orders \
  -H 'Content-Type: application/json' -H 'Idempotency-Key: e2e-decline-1' \
  -d "{\"customer\":{\"customerId\":\"$(new_uuid)\",\"name\":\"Bruno\",\"email\":\"bruno@example.com\",\"phone\":\"11988887777\"},\"shippingAddress\":{\"street\":\"Rua B\",\"number\":\"20\",\"city\":\"Sao Paulo\",\"zipCode\":\"02000-000\",\"country\":\"BR\"},\"payment\":{\"paymentMethod\":\"CREDIT_CARD\",\"installments\":1,\"amount\":20000},\"items\":[{\"productId\":\"$PRODUCT_ID\",\"quantity\":2,\"unitPrice\":10000}]}" \
  | json_field orderId)
echo "orderId=$ORDER_ID"

echo "=== 5/5 acompanhando a saga (esperado: PAYMENT_REJECTED) ==="
STATUS=PENDING_PAYMENT
for _ in $(seq 1 45); do
  STATUS=$(curl -sf "http://localhost:8081/api/v1/orders/$ORDER_ID" | json_field status)
  echo "status=$STATUS"
  [ "$STATUS" != "PENDING_PAYMENT" ] && break
  sleep 2
done

echo "--- verificando compensação de estoque ---"
FINAL_PRODUCT=$(curl -sf "http://localhost:8083/api/v1/products/$PRODUCT_ID")
AVAILABLE=$(echo "$FINAL_PRODUCT" | json_number availableQuantity)
RESERVED=$(echo "$FINAL_PRODUCT" | json_number reservedQuantity)
echo "produto: available=$AVAILABLE reserved=$RESERVED (esperado 10 / 0 após liberar a reserva)"

OK=1
[ "$STATUS" = "PAYMENT_REJECTED" ] || { echo "FALHA: status final = $STATUS (esperado PAYMENT_REJECTED)"; OK=0; }
[ "$AVAILABLE" = "10" ] || { echo "FALHA: available=$AVAILABLE (esperado 10 — reserva não liberada)"; OK=0; }
[ "$RESERVED" = "0" ] || { echo "FALHA: reserved=$RESERVED (esperado 0)"; OK=0; }

if [ "$OK" = "1" ]; then
  echo "E2E OK: pagamento recusado, pedido PAYMENT_REJECTED e reserva de estoque liberada."
  exit 0
fi
echo "E2E FALHOU. Veja $LOG_DIR/*.log"
exit 1
