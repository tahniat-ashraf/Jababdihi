# Operations Runbook

## Health Checks

- Public backend health: `/actuator/health`
- Backend metrics: `/actuator/metrics`
- NGINX routes `/api/*` to `backend-api:8080`.
- PostgreSQL, Redis, backend API, and worker stay on the private Docker network.

## Metrics

The backend exposes basic Micrometer metrics:

- `jababdihi_queue_depth{status=...}`
- `jababdihi_job_runs_total{job=...,status=...}`
- `jababdihi_crawler_fetches_total{publisher=...,status=...}`
- `jababdihi_task_duration{task_type=...}`

## Alerts

`infra/scripts/check-alerts.sh` sends Telegram alerts for:

- API health endpoint not UP.
- PostgreSQL unavailable.
- Daily ingestion failed.
- Backfill stuck longer than `BACKFILL_STUCK_HOURS`.
- Tasks newly failed permanently.
- Queue depth above `QUEUE_DEPTH_ALERT_THRESHOLD`.
- Disk usage above 85% by default.

Install the timers on the VPS:

```bash
sudo cp infra/systemd/jababdihi-alerts@.* /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now jababdihi-alerts@staging.timer
sudo systemctl enable --now jababdihi-alerts@prod.timer
```

Required environment variables in `/opt/jababdihi/<env>/.env`:

```text
TELEGRAM_BOT_TOKEN=
TELEGRAM_CHAT_ID=
QUEUE_DEPTH_ALERT_THRESHOLD=1000
BACKFILL_STUCK_HOURS=6
```

## Backups

`infra/scripts/backup-postgres.sh` performs:

- `pg_dump` from the environment database.
- gzip compression.
- AES-256 encryption with `BACKUP_ENCRYPTION_PASSPHRASE`.
- Local temporary retention of 1 day.
- Optional encrypted object storage upload with AWS-compatible S3 CLI.
- Remote object retention should be configured to 14 days on the bucket.

Install the daily timers:

```bash
sudo cp infra/systemd/jababdihi-backup@.* /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now jababdihi-backup@staging.timer
sudo systemctl enable --now jababdihi-backup@prod.timer
```

Required backup environment variables:

```text
S3_ENDPOINT=
S3_BUCKET=
S3_ACCESS_KEY_ID=
S3_SECRET_ACCESS_KEY=
BACKUP_ENCRYPTION_PASSPHRASE=
```

## VPS Snapshots

Create a weekly VPS snapshot in the hosting provider console. Keep at least the most recent known-good weekly snapshot.

## Monthly Restore Test

Once per month:

1. Download the latest encrypted object backup.
2. Decrypt it with the restore passphrase.
3. Restore it into a temporary PostgreSQL database.
4. Run backend smoke checks against the restored database.
5. Record the restore date, backup object key, and result in the ops log.

Example restore:

```bash
openssl enc -d -aes-256-cbc -pbkdf2 \
  -pass "env:BACKUP_ENCRYPTION_PASSPHRASE" \
  -in backup.sql.gz.enc \
  -out backup.sql.gz
gunzip -c backup.sql.gz | psql "$RESTORE_DATABASE_URL"
```
