#!/usr/bin/env sh
set -eu

ENVIRONMENT="${1:?usage: backup-postgres.sh <staging|prod>}"
BACKUP_ROOT="${BACKUP_ROOT:-/tmp/jababdihi-backups}"
DATE="$(date -u +%Y%m%dT%H%M%SZ)"
BACKUP_DIR="${BACKUP_ROOT}/${ENVIRONMENT}"
PLAIN_FILE="${BACKUP_DIR}/jababdihi-${ENVIRONMENT}-${DATE}.sql.gz"
ENCRYPTED_FILE="${PLAIN_FILE}.enc"

mkdir -p "$BACKUP_DIR"

docker compose --env-file .env -f docker-compose.base.yml -f "docker-compose.${ENVIRONMENT}.yml" exec -T postgres \
  pg_dump -U "$POSTGRES_USER" "$POSTGRES_DB" | gzip > "$PLAIN_FILE"

openssl enc -aes-256-cbc -salt -pbkdf2 \
  -pass "env:BACKUP_ENCRYPTION_PASSPHRASE" \
  -in "$PLAIN_FILE" \
  -out "$ENCRYPTED_FILE"

rm -f "$PLAIN_FILE"
find "$BACKUP_DIR" -type f -name "*.enc" -mtime +1 -delete

if command -v aws >/dev/null 2>&1; then
  AWS_ACCESS_KEY_ID="$S3_ACCESS_KEY_ID" \
  AWS_SECRET_ACCESS_KEY="$S3_SECRET_ACCESS_KEY" \
  aws --endpoint-url "$S3_ENDPOINT" s3 cp "$ENCRYPTED_FILE" "s3://${S3_BUCKET}/${ENVIRONMENT}/"
fi

echo "Created encrypted backup: $ENCRYPTED_FILE"
