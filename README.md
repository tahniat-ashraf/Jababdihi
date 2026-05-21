# Jababdihi

Jababdihi is a public, evidence-first political accountability platform for source-corroborated incidents involving Government and Opposition actors in Bangladesh.

This repository follows the monorepo shape defined in [docs/technical-design.md](docs/technical-design.md). The technical design document is the source of truth for architecture, MVP scope, accepted decisions, and build order.

## Repository Layout

```text
jababdihi/
  frontend/
  backend/
  infra/
  docs/
```

- `frontend/`: Next.js, React, TypeScript, TailwindCSS, shadcn/ui, and TanStack Query.
- `backend/`: Spring Boot 3, Java 21, PostgreSQL, Redis, Quartz, Flyway, and Spring Security.
- `infra/`: Docker Compose, NGINX, deployment scripts, environment templates, and deployment notes.
- `docs/`: Technical design, methodology, implementation notes, and other project documentation.

## Current State

This is the initial monorepo skeleton. Business logic, application scaffolding, CI workflows, deployment scripts, and service configuration have not been implemented yet.

## Local Development

See the area-specific README files:

- [frontend/README.md](frontend/README.md)
- [backend/README.md](backend/README.md)
- [infra/README.md](infra/README.md)

## Project Rules

- Treat `docs/technical-design.md` as the source of truth.
- Keep public APIs read-only.
- Do not add public user accounts, comments, reactions, search, social ranking, social media ingestion, coalition drilldown, or map visualization unless the design doc is updated first.
- Do not commit secrets or real credentials.
- Keep staging and production deployment topology aligned.
