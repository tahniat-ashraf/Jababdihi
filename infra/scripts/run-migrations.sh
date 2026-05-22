#!/usr/bin/env sh
set -eu

ENVIRONMENT="${1:?usage: run-migrations.sh <staging|prod>}"

case "$ENVIRONMENT" in
  staging) OVERLAY="docker-compose.staging.yml" ; ENV_FILE=".env.staging" ;;
  prod)    OVERLAY="docker-compose.prod.yml"    ; ENV_FILE=".env.prod"    ;;
  *) echo "Unknown environment: $ENVIRONMENT" >&2; exit 2 ;;
esac

docker compose \
  --env-file "$ENV_FILE" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  pull backend-api postgres

docker compose \
  --env-file "$ENV_FILE" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  run --rm --no-deps \
  -e SPRING_PROFILES_ACTIVE=api \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  backend-api \
  --spring.main.web-application-type=none \
  --spring.flyway.enabled=true \
  --spring.quartz.auto-startup=false
