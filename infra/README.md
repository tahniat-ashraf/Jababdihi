# Infra

Infrastructure workspace for Jababdihi.

## Planned Scope

- Docker Compose service definitions.
- NGINX configuration.
- Deployment scripts.
- Environment templates.
- GitHub Actions deployment notes.
- Vercel frontend deployment notes.
- Staging and production environment parity documentation.

## Deployment Targets

- Frontend: Vercel for previews and production.
- Backend/data/AI: Hostinger KVM VPS initially.
- Core VPS services:
  - backend API for staging and production.
  - backend worker for staging and production.
  - PostgreSQL for staging and production.
  - Redis for staging and production.
  - Ollama/private AI runtime.

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
