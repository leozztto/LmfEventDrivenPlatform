#!/usr/bin/env bash
# Sobe a infraestrutura local (Postgres com os 3 bancos + Kafka).
set -euo pipefail

COMPOSE_FILE="$(dirname "$0")/../infrastructure/docker/docker-compose.yml"

docker compose -f "$COMPOSE_FILE" up -d

echo "Aguardando Postgres e Kafka ficarem prontos..."
docker compose -f "$COMPOSE_FILE" exec -T postgres bash -c 'until pg_isready -U postgres; do sleep 1; done'
until docker compose -f "$COMPOSE_FILE" exec -T kafka kafka-topics --bootstrap-server localhost:9092 --list >/dev/null 2>&1; do sleep 1; done

echo "Infra pronta. Bancos: orderservice, paymentservice, inventoryservice. Kafka: localhost:9092"
