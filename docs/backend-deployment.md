# Backend Deployment

How to build, deploy, operate, and recover the backend on the Hostinger VPS.

---

## How Docker images are built

One image is built per commit. It runs as both the `api` process and the `worker`
process — the Spring profile is selected at container start via `SPRING_PROFILES_ACTIVE`.

```
backend/Dockerfile
  stage 1 (build):  maven:3.9-eclipse-temurin-25  →  mvn package
  stage 2 (runtime): eclipse-temurin:25-jre        →  java -jar backend.jar
```

The image is pushed to GitHub Container Registry:

```
ghcr.io/tahniat-ashraf/jababdihi-backend:<image-tag>
```

Image tags follow the pattern `sha-<7-char-commit-hash>`.
Release tags (e.g. `v1.2.3`) are used for production deploys.

---

## Compose topology

```
infra/
  docker-compose.base.yml         — service definitions shared by all envs
  docker-compose.staging.yml      — staging volume names + Spring profile overlay
  docker-compose.prod.yml         — production volume names + Spring profile overlay
```

Services per environment:

| Service | Network | Publicly reachable |
|---------|---------|--------------------|
| `nginx` | public + private | Yes (port 80 / 8081 for staging) |
| `backend-api` | private only | Through nginx only (`/api/*`, `/actuator/health`) |
| `backend-worker` | private only | No |
| `postgres` | private only | No |
| `redis` | private only | No |

---

## How staging deploy works

1. GitHub Actions builds the image, pushes to GHCR with tag `sha-<hash>`.
2. The workflow SSH-es into the staging VPS.
3. It runs `deploy-staging.sh sha-<hash>` from the `infra/` directory.
4. The script:
   - Pulls the new image.
   - Runs Flyway migrations in a one-shot container.
   - Starts all services with `docker compose up -d --wait`.
   - Polls `/actuator/health` through NGINX until `UP` or times out.

Staging Spring profiles: `api,staging` and `worker,staging`.

Staging jobs (crawler, backfill, daily ingestion) are **disabled by default**.
Use the admin manual-trigger endpoints to run ingestion tasks on staging.

---

## How production deploy works

1. After merging to `main`, GitHub Actions builds a release image.
2. The `production-migrations` environment requires manual approval.
3. After approval, Flyway migrations run against the production database.
4. The `production` environment also requires manual approval.
5. After approval, `deploy-prod.sh <tag>` runs the production update.
6. The script follows the same pull → migrate → up → health-check flow.

Production Spring profiles: `api,prod` and `worker,prod`.

---

## Running deploy scripts manually

All deploy scripts must be run from the `infra/` directory on the VPS.

**Staging:**

```bash
ssh deploy@<STAGING_VPS_IP>
cd /opt/jababdihi/staging/repo/infra
./scripts/deploy-staging.sh sha-abc1234
```

**Production:**

```bash
ssh deploy@<PROD_VPS_IP>
cd /opt/jababdihi/prod/repo/infra
./scripts/deploy-prod.sh v1.2.3
```

Replace `sha-abc1234` / `v1.2.3` with the actual image tag.

---

## Checking logs

```bash
# Staging — all services
docker compose --env-file .env.staging \
  -f docker-compose.base.yml -f docker-compose.staging.yml \
  logs -f

# Staging — api only
docker compose --env-file .env.staging \
  -f docker-compose.base.yml -f docker-compose.staging.yml \
  logs -f backend-api

# Production — tail the last 200 lines then follow
docker compose --env-file .env.prod \
  -f docker-compose.base.yml -f docker-compose.prod.yml \
  logs --tail=200 -f backend-api
```

Logs are structured JSON (configured via `logback-spring.xml`).

---

## Checking health

Health endpoint is exposed through NGINX:

```bash
# Staging
curl http://127.0.0.1:8081/actuator/health

# Production
curl http://127.0.0.1:80/actuator/health
# or via domain
curl https://api.<YOUR_DOMAIN>/actuator/health
```

Expected response when healthy:

```json
{"status":"UP","components":{"db":{"status":"UP"},"redis":{"status":"UP"},"diskSpace":{"status":"UP"}}}
```

Metrics endpoint (internal only, accessible from the VPS):

```bash
# Direct to backend container port — not exposed through NGINX
CONTAINER_IP=$(docker inspect -f '{{range.NetworkSettings.Networks}}{{.IPAddress}}{{end}}' \
  $(docker compose --env-file .env.prod -f docker-compose.base.yml -f docker-compose.prod.yml ps -q backend-api))
curl "http://$CONTAINER_IP:8080/actuator/metrics"
```

---

## Rolling back to a previous image tag

1. Find the previous working image tag from GHCR or the GitHub Actions run history.
2. Run the deploy script with that tag:

   ```bash
   # Staging
   ./scripts/deploy-staging.sh sha-previous

   # Production
   ./scripts/deploy-prod.sh v1.1.0
   ```

3. The script will pull the old image, skip any already-applied Flyway migrations
   (Flyway is append-only; applied migrations will not re-run), restart services,
   and verify health.

**If a migration already ran in production and is incompatible with the old code:**

Follow the forward-fix policy defined in `docs/technical-design.md` — write a new
migration that repairs the schema rather than attempting a rollback. Do not use
`flyway repair` or destructive rollbacks on production data.

---

## Updating the repository on the VPS

When `infra/` files change (new compose configs, updated scripts):

```bash
ssh deploy@<VPS_IP>
cd /opt/jababdihi/staging/repo   # or /prod/repo
git pull origin main
# Then re-run the deploy script to pick up compose changes
cd infra
./scripts/deploy-staging.sh <image-tag>
```

---

## Stopping all services

```bash
# Staging
docker compose --env-file .env.staging \
  -f docker-compose.base.yml -f docker-compose.staging.yml \
  down

# Production (this stops the public API — use with care)
docker compose --env-file .env.prod \
  -f docker-compose.base.yml -f docker-compose.prod.yml \
  down
```

Add `--volumes` to also destroy the data volumes (destructive — do not use on production).
