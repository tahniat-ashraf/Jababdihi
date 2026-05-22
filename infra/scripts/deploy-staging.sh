#!/usr/bin/env sh
# Deploy the staging stack on the VPS.
#
# Usage (from /opt/jababdihi/staging/):
#   ./infra/scripts/deploy-staging.sh <image-tag>
#
# Requirements:
#   - /opt/jababdihi/staging/infra/.env.staging must exist (copy .env.staging.example)
#   - Docker with Compose plugin v2.1+
#   - curl available on the host
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
INFRA_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$INFRA_DIR"

IMAGE_TAG="${1:?usage: deploy-staging.sh <image-tag>}"
ENV_FILE=".env.staging"
COMPOSE_FLAGS="--env-file $ENV_FILE -f docker-compose.base.yml -f docker-compose.staging.yml"

if [ ! -f "$ENV_FILE" ]; then
  echo "ERROR: $ENV_FILE not found in $(pwd)" >&2
  echo "  Copy infra/.env.staging.example to $ENV_FILE and fill in values." >&2
  exit 1
fi

export BACKEND_IMAGE="ghcr.io/tahniat-ashraf/jababdihi-backend:${IMAGE_TAG}"
echo "[staging] Image: $BACKEND_IMAGE"

echo "[staging] Pulling images..."
# shellcheck disable=SC2086
docker compose $COMPOSE_FLAGS pull backend-api backend-worker nginx postgres redis

echo "[staging] Running Flyway migrations..."
# shellcheck disable=SC2086
docker compose $COMPOSE_FLAGS run --rm --no-deps \
  -e SPRING_PROFILES_ACTIVE=api,staging \
  -e SPRING_MAIN_WEB_APPLICATION_TYPE=none \
  backend-api \
  --spring.main.web-application-type=none \
  --spring.flyway.enabled=true \
  --spring.quartz.auto-startup=false

echo "[staging] Starting services (waiting for health checks)..."
# --wait blocks until all service health checks pass or any service exits with error.
# shellcheck disable=SC2086
docker compose $COMPOSE_FLAGS up -d --remove-orphans --wait

echo "[staging] Verifying health endpoint through NGINX..."
NGINX_PORT="$(grep -E '^NGINX_HTTP_PORT=' "$ENV_FILE" | cut -d= -f2 | tr -d '[:space:]')"
NGINX_PORT="${NGINX_PORT:-8081}"
HEALTH_URL="http://127.0.0.1:${NGINX_PORT}/actuator/health"
TRIES=0
MAX_TRIES=12  # 60 seconds total

while [ "$TRIES" -lt "$MAX_TRIES" ]; do
  if curl -fsS "$HEALTH_URL" 2>/dev/null | grep -q '"UP"'; then
    echo "[staging] Health check passed."
    break
  fi
  TRIES=$((TRIES + 1))
  if [ "$TRIES" -ge "$MAX_TRIES" ]; then
    echo "[staging] ERROR: Health check at $HEALTH_URL failed after $((MAX_TRIES * 5))s." >&2
    # shellcheck disable=SC2086
    docker compose $COMPOSE_FLAGS logs --tail=50 backend-api
    exit 1
  fi
  echo "[staging] Waiting for health... ($((TRIES * 5))/$((MAX_TRIES * 5))s)"
  sleep 5
done

echo "[staging] Deploy complete."
# shellcheck disable=SC2086
docker compose $COMPOSE_FLAGS ps
