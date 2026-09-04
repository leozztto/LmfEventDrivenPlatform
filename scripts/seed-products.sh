#!/usr/bin/env bash
# Carga de produtos para o InventoryService (catálogo maior que o do seed-local.sh).
# Idempotente: produtos com SKU já existente são ignorados (409) e o script continua.
# Uso: ./scripts/seed-products.sh [http://localhost:8083]
set -uo pipefail

INVENTORY_URL="${1:-http://localhost:8083}"

seed_product() {
  local sku="$1" name="$2" description="$3" price="$4" stock="$5"
  local http_code body
  body="$(mktemp)"
  http_code="$(curl -s -o "$body" -w '%{http_code}' -X POST "$INVENTORY_URL/api/v1/products" \
    -H 'Content-Type: application/json' \
    -d "{\"sku\":\"$sku\",\"name\":\"$name\",\"description\":\"$description\",\"price\":$price,\"initialStock\":$stock}")"

  case "$http_code" in
    200|201)
      echo "$sku: $(grep -oE '"id"[[:space:]]*:[[:space:]]*"[^"]*"' "$body" | sed -E 's/.*"([^"]*)"$/\1/')"
      ;;
    409)
      echo "$sku: ja existe, ignorado"
      ;;
    *)
      echo "$sku: falhou (HTTP $http_code) - $(cat "$body")" >&2
      ;;
  esac
  rm -f "$body"
}

echo "Semeando catalogo de produtos em $INVENTORY_URL ..."

# Informatica
seed_product "SKU-NOTE"       "Notebook"              "Notebook 15\" 16GB RAM 512GB SSD"     5000.00 100
seed_product "SKU-NOTE-GAMER" "Notebook Gamer"        "Notebook gamer 32GB RAM 1TB SSD RTX"  9500.00  40
seed_product "SKU-MOUSE"      "Mouse"                 "Mouse otico USB"                        120.00 200
seed_product "SKU-MOUSE-GAMER" "Mouse Gamer"          "Mouse gamer RGB 16000 DPI"              280.00 150
seed_product "SKU-KB"         "Teclado"               "Teclado membrana ABNT2"                 300.00 150
seed_product "SKU-KB-MEC"     "Teclado Mecanico"      "Teclado mecanico RGB switch red"         650.00  80
seed_product "SKU-MONITOR-24" "Monitor 24 polegadas"  "Monitor Full HD IPS 24\""               900.00  90
seed_product "SKU-MONITOR-27" "Monitor 27 polegadas"  "Monitor QHD IPS 27\""                  1400.00  60
seed_product "SKU-HEADSET"    "Headset"               "Headset estereo com microfone"          250.00 120
seed_product "SKU-WEBCAM"     "Webcam"                "Webcam Full HD 1080p"                   220.00 100

# Armazenamento e acessorios
seed_product "SKU-SSD-480"    "SSD 480GB"             "SSD SATA 480GB"                         280.00 130
seed_product "SKU-SSD-1TB"    "SSD 1TB"               "SSD NVMe 1TB"                            550.00  90
seed_product "SKU-HD-2TB"     "HD Externo 2TB"        "HD externo portatil 2TB USB 3.0"         420.00  70
seed_product "SKU-PENDRIVE"   "Pendrive 64GB"         "Pendrive USB 3.0 64GB"                    45.00  300
seed_product "SKU-CARREGADOR" "Carregador USB-C"      "Carregador rapido USB-C 65W"              90.00  200

# Redes
seed_product "SKU-ROTEADOR"   "Roteador Wi-Fi"        "Roteador wireless AC1200"                220.00  85
seed_product "SKU-SWITCH-8P"  "Switch 8 portas"       "Switch de rede Gigabit 8 portas"         180.00  60
seed_product "SKU-CABO-REDE"  "Cabo de rede Cat6"      "Cabo de rede Cat6 5 metros"                35.00  400

# Moveis e escritorio
seed_product "SKU-CADEIRA"    "Cadeira de Escritorio" "Cadeira ergonomica com apoio de braco"   750.00  50
seed_product "SKU-MESA"       "Mesa para Escritorio"  "Mesa de escritorio 120x60cm"              600.00  40

echo "Carga concluida. Use os UUIDs listados acima como productId ao criar pedidos."
