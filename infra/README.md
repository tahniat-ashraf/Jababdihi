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

No Docker Compose files or deployment scripts have been added yet.

When infrastructure files are added, document the exact validation command here. Expected commands will likely include:

```bash
docker compose config
```

## Boundaries

- Keep staging and production architecture identical except for documented resource and configuration differences.
- Do not expose Ollama, PostgreSQL, Redis, worker services, or internal APIs publicly.
- Do not commit secrets, real credentials, or production environment values.
- OpenSearch, Kafka, Kubernetes, and full observability stacks are outside MVP scope.
