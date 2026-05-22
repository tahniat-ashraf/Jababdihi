# Infra

Infrastructure workspace for Jababdihi.

## Scope

- Local, staging, and production Docker Compose service definitions.
- NGINX routing for the backend API.
- GitHub Actions backend build, image, migration, and deploy workflows.
- Vercel frontend deployment notes.
- Telegram alert and PostgreSQL backup scripts.
- Environment templates for local, staging, and production.

## Deployment Targets

- Frontend: Vercel for previews and production.
- Backend/data/AI: Hostinger KVM VPS initially.
- Core VPS services:
  - backend API for staging and production.
  - backend worker for staging and production.
  - PostgreSQL for staging and production.
  - Redis for staging and production.
  - Ollama/private AI runtime.

## Staging And Production Compose

Staging and production use the same topology:

- `nginx`
- `backend-api`
- `backend-worker`
- `postgres`
- `redis`
- optional private `ollama`

The environment-specific Compose overlays only change labels and named volumes. Separate databases are configured through each environment's `.env`.
For MVP, the NGINX container listens on HTTP only; terminate TLS at the VPS provider, a host-level reverse proxy, or Cloudflare before forwarding to `NGINX_HTTP_PORT`.

Validate staging:

```bash
docker compose --env-file infra/.env.staging.example \
  -f infra/docker-compose.base.yml \
  -f infra/docker-compose.staging.yml config
```

Validate production:

```bash
docker compose --env-file infra/.env.prod.example \
  -f infra/docker-compose.base.yml \
  -f infra/docker-compose.prod.yml config
```

## CI/CD

Backend CI runs on pull requests and pushes to `main` or `staging`:

- `mvn spotless:check`
- `mvn test`
- `mvn package`

Backend deployment workflow:

- Builds and pushes one backend Docker image to GHCR.
- Runs the same image as `backend-api` and `backend-worker`, with profile differences supplied by Compose.
- Deploys staging from pull requests from this repository, the `staging` branch, or manual workflow dispatch.
- Deploys production from `main` or manual workflow dispatch.
- Runs Flyway automatically before staging deploy.
- Runs production Flyway migrations in a separate `production-migrations` GitHub Environment, so approval can be required before migrations execute.
- Deploys production in the `production` GitHub Environment, so final deploy approval can be required separately.

Required GitHub repository environments:

- `staging`
- `production-migrations`
- `production`

Required GitHub secrets:

```text
GHCR_TOKEN
STAGING_VPS_HOST
STAGING_VPS_PORT
STAGING_VPS_USER
STAGING_VPS_SSH_KEY
STAGING_DEPLOY_PATH
PROD_VPS_HOST
PROD_VPS_PORT
PROD_VPS_USER
PROD_VPS_SSH_KEY
PROD_DEPLOY_PATH
```

On the VPS, each deploy path should contain a real `.env` copied from:

- `infra/.env.staging.example`
- `infra/.env.prod.example`

## Frontend Deployment

See [Vercel setup](vercel.md).

Summary:

- Vercel preview deployments run for pull requests.
- Preview uses the staging API.
- Vercel production deploys from `main`.
- Production uses the production API.

## Operations

See [Operations runbook](operations.md).

Included MVP operational pieces:

- Structured JSON backend logs.
- Actuator health and metrics endpoints.
- Queue, job, crawler, and task duration metrics.
- Telegram alert script.
- Daily encrypted PostgreSQL backups.
- Weekly VPS snapshot and monthly restore-test documentation.

## Local Setup

Copy the example environment file before starting local services:

```bash
cp infra/.env.example infra/.env.local
```

Validate the local Compose file:

```bash
docker compose --env-file infra/.env.local -f infra/docker-compose.local.yml config
```

Start PostgreSQL, Redis, backend API, and backend worker:

```bash
docker compose --env-file infra/.env.local -f infra/docker-compose.local.yml up postgres redis backend-api backend-worker
```

Start the optional frontend service:

```bash
docker compose --env-file infra/.env.local -f infra/docker-compose.local.yml --profile frontend up frontend
```

Start the optional Ollama placeholder service:

```bash
docker compose --env-file infra/.env.local -f infra/docker-compose.local.yml --profile ollama up ollama
```

Ollama is present only as a local placeholder. No AI processing is wired to it yet.

## Local Services

- `postgres`: PostgreSQL for local development.
- `redis`: Redis for local caching/rate-limit development.
- `backend-api`: Spring Boot API process with the `api` profile.
- `backend-worker`: Spring Boot worker process with the `worker` profile.
- `frontend`: optional Next.js development server.
- `ollama`: optional local AI runtime placeholder, disabled unless the `ollama` profile is used.

## Boundaries

- Keep staging and production architecture identical except for documented resource and configuration differences.
- Do not expose Ollama, PostgreSQL, Redis, worker services, or internal APIs publicly.
- Do not commit secrets, real credentials, or production environment values.
- OpenSearch, Kafka, Kubernetes, and full observability stacks are outside MVP scope.
