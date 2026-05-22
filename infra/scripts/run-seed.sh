#!/usr/bin/env sh
# Insert deterministic staging smoke-test data into the staging database.
#
# Usage (from the infra/ directory on the staging VPS):
#   ./scripts/run-seed.sh staging
#
# The seeder is idempotent — it detects existing seed rows and skips if already
# present, so it is safe to run on every CI deploy.
#
# THIS SCRIPT INTENTIONALLY DOES NOT SUPPORT A "prod" ARGUMENT.
# Seed data must never be inserted into the production database.
set -eu

ENVIRONMENT="${1:?usage: run-seed.sh <staging>}"

case "$ENVIRONMENT" in
  staging)
    OVERLAY="docker-compose.staging.yml"
    ENV_FILE=".env.staging"
    ;;
  prod | production)
    echo "ERROR: run-seed.sh refuses to run against production." >&2
    echo "       Seed data must not be inserted into the production database." >&2
    exit 2
    ;;
  *)
    echo "Unknown environment: $ENVIRONMENT" >&2
    echo "Usage: run-seed.sh <staging>" >&2
    exit 2
    ;;
esac

if [ ! -f "$ENV_FILE" ]; then
  echo "ERROR: $ENV_FILE not found in $(pwd)" >&2
  exit 1
fi

echo "[seed] Running staging seed (idempotent — safe to re-run)..."

docker compose \
  --env-file "$ENV_FILE" \
  -f docker-compose.base.yml \
  -f "$OVERLAY" \
  run --rm --no-deps \
  -e SPRING_PROFILES_ACTIVE=api,staging \
  backend-api \
  --app.seed=staging

echo "[seed] Done."
