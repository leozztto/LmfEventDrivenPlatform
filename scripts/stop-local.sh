#!/usr/bin/env bash
# Derruba a infraestrutura local.
set -euo pipefail

COMPOSE_FILE="$(dirname "$0")/../infrastructure/docker/docker-compose.yml"

docker compose -f "$COMPOSE_FILE" down -v
echo "Infra parada."
