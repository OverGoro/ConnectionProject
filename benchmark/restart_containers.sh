#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE=${1:-docker-compose.yml}

echo "Restarting containers using $COMPOSE_FILE"
docker-compose -f "$COMPOSE_FILE" down --remove-orphans
docker-compose -f "$COMPOSE_FILE" up -d

echo "Containers restarted"
