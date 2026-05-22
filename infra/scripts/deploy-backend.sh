#!/usr/bin/env sh
set -eu

ENVIRONMENT="${1:?usage: deploy-backend.sh <staging|prod> <backend-image>}"
BACKEND_IMAGE_VALUE="${2:?usage: deploy-backend.sh <staging|prod> <backend-image>}"

case "$ENVIRONMENT" in
  staging) OVERLAY="docker-compose.staging.yml" ;;
  prod) OVERLAY="docker-compose.prod.yml" ;;
  *) echo "Unknown environment: $ENVIRONMENT" >&2; exit 2 ;;
esac

export BACKEND_IMAGE="$BACKEND_IMAGE_VALUE"

docker compose \
  --env-file ".env" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  pull backend-api backend-worker nginx redis postgres

docker compose \
  --env-file ".env" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  up -d --remove-orphans

docker compose \
  --env-file ".env" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  ps
