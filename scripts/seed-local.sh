#!/usr/bin/env bash
# Cadastra alguns produtos no InventoryService (necessário antes de criar um pedido).
# Uso: ./scripts/seed-local.sh [http://localhost:8083]
set -euo pipefail

INVENTORY_URL="${1:-http://localhost:8083}"

seed_product() {
  local sku="$1" name="$2" price="$3" stock="$4"
  curl -sf -X POST "$INVENTORY_URL/api/v1/products" \
    -H 'Content-Type: application/json' \
    -d "{\"sku\":\"$sku\",\"name\":\"$name\",\"description\":\"$name\",\"price\":$price,\"initialStock\":$stock}" \
    | grep -oE '"(id|sku)"[[:space:]]*:[[:space:]]*"[^"]*"' | sed -E 's/.*"([^"]*)"$/\1/' | paste -sd' ' -
}

echo "Semeando produtos em $INVENTORY_URL ..."
seed_product "SKU-NOTE" "Notebook" 5000 100
seed_product "SKU-MOUSE" "Mouse" 120 200
seed_product "SKU-KB" "Teclado" 300 150
echo "Pronto. Use os UUIDs acima como productId ao criar pedidos."
