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

- `frontend/`: Next.js, React, TypeScript, TailwindCSS, shadcn/ui.
- `backend/`: Spring Boot 3, Java 25, PostgreSQL, Redis, Quartz, Flyway.
- `infra/`: Docker Compose, environment templates, deployment scripts.
- `docs/`: Technical design, methodology, implementation notes.

## Project Rules

- Treat `docs/technical-design.md` as the source of truth.
- Keep public APIs read-only.
- Do not add public user accounts, comments, reactions, search, social ranking, social media
  ingestion, coalition drilldown, or map visualization unless the design doc is updated first.
- Do not commit secrets or real credentials.
- Keep staging and production deployment topology aligned.

---

## Local Development

### Prerequisites

- Docker (for PostgreSQL and Redis)
- Java 25 + Maven (or use the Maven wrapper in `backend/`)
- Node.js 20+ and npm

### 1. Start infrastructure

```bash
docker compose -f infra/docker-compose.local.yml up -d postgres redis
```

### 2. Configure environment (first time only)

Copy and edit the example files:

```bash
cp infra/backend.env.example backend/.env
cp infra/frontend.env.example frontend/.env.local
```

The defaults work for local development without any changes.

### 3. Start the backend API

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=api,local
```

The API starts on **http://localhost:8080**. Flyway migrations run automatically on startup.

### 4. Seed test data

Run this once after the backend is up (or after wiping the database):

```bash
cd backend
./mvnw spring-boot:run \
  -Dspring-boot.run.profiles=api,seed \
  -Dspring-boot.run.arguments="--app.seed=staging"
```

This inserts 31 sample incidents (21 Government + 5 Opposition published, plus 5 non-public) and
exits automatically. Seeding is idempotent — re-running when data already exists is a no-op.

Seeded data covers:

- Government and Opposition incidents with Bangla and English translations
- Confidence scores across low / moderate / high bands
- Multiple categories per incident
- Multiple sources per incident, including incidents with 5 sources (to exercise "+N more")
- Non-public incidents (PENDING_REVIEW, REJECTED, ARCHIVED, UNKNOWN actor) that must not appear
  in the public feed

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend starts on **http://localhost:3000** and proxies `/api/*` calls to the backend.

---

## Running the Browser Smoke Test

The smoke test verifies the full end-to-end flow against the locally running stack. Complete
steps 1–5 above before running it.

### Install Playwright browsers (first time only)

```bash
cd frontend
npx playwright install chromium
```

### Run the tests

```bash
cd frontend
npm run test:e2e
```

The test suite covers:

1. Bangla route (`/bn`) loads without error
2. Government feed loads by default with incident cards visible
3. Opposition switch updates the feed
4. English route (`/en`) shows English UI labels
5. Category filter — deselecting all shows an empty state
6. Source chips show the first 3 sources and a `+N more` chip for incidents with 4+ sources
7. Confidence gauge shows a numeric 0–100 score
8. Infinite scroll fetches the next page when scrolling to the bottom
9. Public feed excludes non-public incidents (PENDING_REVIEW, REJECTED, ARCHIVED, UNKNOWN actor)
10. Source anchor tags have `target="_blank"` and `rel="noopener noreferrer"`

### Run against a different URL

```bash
BASE_URL=http://staging.example.com cd frontend && npm run test:e2e
```

---

## Backend Checks

```bash
cd backend
./mvnw test
./mvnw package
./mvnw spotless:check
```

## Frontend Checks

```bash
cd frontend
npm run lint
npm run typecheck
npm run build
```

## Infra Checks

```bash
docker compose -f infra/docker-compose.local.yml config
```
