#!/usr/bin/env sh
set -eu

ENVIRONMENT="${1:?usage: check-alerts.sh <staging|prod>}"
API_HEALTH_URL="${API_HEALTH_URL:-http://127.0.0.1/actuator/health}"
QUEUE_DEPTH_ALERT_THRESHOLD="${QUEUE_DEPTH_ALERT_THRESHOLD:-1000}"
DISK_USAGE_ALERT_THRESHOLD="${DISK_USAGE_ALERT_THRESHOLD:-85}"
BACKFILL_STUCK_HOURS="${BACKFILL_STUCK_HOURS:-6}"

alert() {
  ./scripts/telegram-alert.sh "Jababdihi ${ENVIRONMENT}: $1"
}

if ! curl -fsS "$API_HEALTH_URL" | grep -q '"status":"UP"'; then
  alert "API down or health endpoint not UP"
fi

if ! docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres pg_isready -U "$POSTGRES_USER" -d "$POSTGRES_DB" >/dev/null 2>&1; then
  alert "database down"
fi

QUEUE_DEPTH="$(docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from processing_tasks where status in ('PENDING','FAILED_RETRYABLE');" 2>/dev/null || echo 0)"
if [ "$QUEUE_DEPTH" -gt "$QUEUE_DEPTH_ALERT_THRESHOLD" ]; then
  alert "queue depth above threshold: ${QUEUE_DEPTH}"
fi

FAILED_PERMANENTLY="$(docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from processing_tasks where status = 'FAILED_PERMANENTLY' and updated_at > now() - interval '15 minutes';" 2>/dev/null || echo 0)"
if [ "$FAILED_PERMANENTLY" -gt 0 ]; then
  alert "task failed permanently in the last 15 minutes"
fi

DAILY_FAILED="$(docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from ingestion_job_runs where job_type = 'daily_rolling_ingestion' and status = 'FAILED' and created_at > now() - interval '1 day';" 2>/dev/null || echo 0)"
if [ "$DAILY_FAILED" -gt 0 ]; then
  alert "daily ingestion failed"
fi

BACKFILL_STUCK="$(docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from ingestion_job_runs where job_type = 'hourly_backfill' and status = 'RUNNING' and started_at < now() - interval '${BACKFILL_STUCK_HOURS} hours';" 2>/dev/null || echo 0)"
if [ "$BACKFILL_STUCK" -gt 0 ]; then
  alert "backfill stuck for more than ${BACKFILL_STUCK_HOURS} hours"
fi

DISK_USAGE="$(df -P / | awk 'NR==2 {gsub(\"%\", \"\", $5); print $5}')"
if [ "$DISK_USAGE" -gt "$DISK_USAGE_ALERT_THRESHOLD" ]; then
  alert "disk usage above ${DISK_USAGE_ALERT_THRESHOLD}%: ${DISK_USAGE}%"
fi
