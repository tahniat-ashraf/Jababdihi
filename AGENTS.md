# AGENTS.md

## Project Goal

Build **Jababdihi**, a public, evidence-first political accountability platform described in `docs/technical-design.md`.

Always treat `docs/technical-design.md` as the source of truth for architecture, scope, accepted decisions, MVP constraints, and build order.

## Product Principles

- Evidence-first, not AI-first.
- AI assists extraction, summarization, translation, and deduplication; it does not determine truth, guilt, or legal verification.
- Confidence means source corroboration strength, not legal proof.
- Use neutral wording such as “reported,” “alleged,” “sources reported,” and “corroborated by sources.”
- Do not introduce outrage, virality, social ranking, or partisan language.
- Preserve source transparency: every published incident must show linked source evidence.

## Operating Rules

- Push directly to `main`. Do not open pull requests unless explicitly asked.
- Do not build features that are explicitly out of MVP scope.
- Do not add public user registration, login, comments, reactions, public search, popularity-based ranking, social media ingestion, coalition drilldown, or map visualization unless the design doc is updated first.
- Do not expose internal services publicly.
- Do not commit secrets.
- Prefer simple, boring, auditable code over clever abstractions.
- If a task conflicts with `docs/technical-design.md`, note the conflict in the commit message.

## Repository Structure

Use the monorepo structure:

```text
jababdihi/
  frontend/
  backend/
  infra/
  docs/
```

- `frontend/`: Next.js, React, TypeScript, TailwindCSS, shadcn/ui.
- `backend/`: Spring Boot, Java 21, PostgreSQL, Redis, Quartz, Flyway.
- `infra/`: Docker Compose, NGINX, deployment scripts, environment templates.
- `docs/`: technical design, methodology, implementation notes.

## Frontend Rules

- Use Next.js App Router.
- Use React, TypeScript, TailwindCSS, and shadcn/ui.
- Use Vercel preview deployments for branch/PR UX review and Vercel production deployment from `main`.
- The public UI must support Bangla and English from MVP.
- Actor role and language are reflected in the URL.
- Category selections remain local frontend state and are not encoded in the URL.
- Government is the default feed.
- Government is color-coded blue; Opposition is color-coded red.
- Use infinite scroll behavior backed by page-number API calls.
- Source links open directly in a new tab with `noopener noreferrer`; do not add redirect/tracking endpoints.
- Do not add popularity-based ranking or detailed click tracking.
- Do not call AI during public page rendering.

## Backend Rules

- Use Java 25 and Spring Boot 3.
- Use one backend codebase with separate runtime processes/profiles:
  - `api`: public API and admin/internal API.
  - `worker`: ingestion, AI processing, deduplication, scheduled jobs.
- Public APIs must be read-only.
- Internal machine-to-machine APIs must require API-key authentication.
- Admin UI/API access uses Basic Auth for MVP.
- Use PostgreSQL as the primary database.
- Use pgvector for embeddings/internal deduplication.
- Use Redis for caching/rate limiting only, not as the durable task queue.
- Use PostgreSQL-backed `processing_tasks` as the durable task queue.
- Use Quartz Scheduler with PostgreSQL-backed job state.
- Use Flyway versioned SQL migrations.
- Use forward-fix migration policy; do not rely on destructive rollbacks.
- Backend Java code must follow the Google Java Style Guide.
- Use the backend Maven Spotless configuration to apply and check Google Java Format.
- Add service-level tests where practical.

## Data and Publishing Rules

- Do not publish incidents unless they satisfy the MVP auto-publish criteria or are manually published.
- Unknown actor incidents must remain internal and excluded from the public feed.
- Store extracted source text internally for admin review, auditability, reprocessing, and deduplication debugging.
- Do not display full article text publicly.
- Store localized incident titles/summaries in `incident_translations`.
- Normalize locations to at least district level whenever possible.
- Keep raw source links, publisher metadata, and publication dates.

## AI Rules

- Ollama is the primary self-hosted AI runtime.
- Cloud fallback must be designed as configurable but disabled by default in MVP.
- Ollama, model endpoints, embedding services, PostgreSQL, Redis, and internal APIs must never be publicly exposed.
- Store model name, prompt version, extraction confidence, and processing timestamp for AI outputs.
- Benchmark Qwen-class models before final extraction/summarization model selection.
- Benchmark BGE-M3 vs multilingual-e5-large before final embedding model selection.
- Do not make guilt/truth claims from AI output.
- AI-generated summaries must use neutral allegation/corroboration wording.

## Ingestion Rules

- MVP ingestion is RSS-first with direct scraping fallback per publisher.
- MVP sources are allowlisted newspapers and official sources only.
- Exclude Facebook, YouTube, blogs, unknown websites, and screenshot-only sources from MVP ingestion.
- Use canonical URL normalization and content hash deduplication at crawl time.
- Daily production ingestion uses a rolling 3-day window.
- Backfill starts on 2026-02-17, runs hourly, processes one historical day per run, and idles after catch-up.
- Staging crawler/backfill/daily schedules are disabled by default; staging ingestion/AI tasks are manual-trigger only.

## Security Rules

- Never commit secrets or real credentials.
- Use environment variables and deployment secrets.
- Public endpoints are read-only.
- Internal endpoints require API key.
- Admin UI uses Basic Auth for MVP.
- PostgreSQL, Redis, Ollama, and worker services bind to private Docker/server networks only.
- Add rate limiting where public endpoints could be abused.
- Use HTTPS in deployed environments.
- Use source allowlists and URL sanitation to reduce SSRF risk in crawlers.

## CI/CD Rules

- Use GitHub pull requests for reviewable changes.
- `main` and the `production` promotion branch must be protected.
- Require PR review and passing checks before promotion to backend production.
- Frontend:
  - Vercel creates preview deployments for feature branch pushes and PRs.
  - Vercel deploys production frontend from `main`.
- Backend:
  - GitHub Actions builds/tests backend and deploys Docker Compose services over SSH.
  - Staging deploys from PRs, pushes to `main`, and manual staging dispatch.
  - Production backend deploys only after `git push origin main:production` or manual production dispatch, with GitHub Environment approval.
- Staging and production must use the same Docker images, service topology, Flyway migrations, API contracts, and AI integration shape.
- Staging and production must use separate databases.

## First-Time Setup (after cloning)

Run once to install the local secret-scanning git hooks:

```bash
./infra/scripts/setup-git-hooks.sh
```

This installs a `pre-commit` hook and a `pre-push` hook, both backed by
[gitleaks](https://github.com/gitleaks/gitleaks). Install gitleaks first:

```bash
brew install gitleaks   # macOS
# or download from https://github.com/gitleaks/gitleaks/releases
```

The hooks are non-blocking if gitleaks is not installed (a warning is printed
instead). The CI `secret-scan` job is the hard gate — it runs on every push
and pull request and will fail the build if a secret is detected.

## Checks Before Finishing a Task

Run the relevant checks when possible.

### Frontend

```bash
npm run lint
npm run typecheck
npm run build
```

### Backend

```bash
./mvnw test
./mvnw package
./mvnw spotless:check
```

### Infra

```bash
docker compose config
```

If a command cannot be run, explain why in the PR summary.

## PR Summary Format

Every PR should include:

1. What changed
2. Why it changed
3. Files/modules touched
4. Tests/checks run
5. Screenshots for frontend changes, if applicable
6. Migration notes, if database changes were made
7. Any deviations from `docs/technical-design.md`

## Initial Build Order

Follow the build order in `docs/technical-design.md`. Do not attempt to build the whole application in one PR.

Recommended first PR:

1. Create monorepo skeleton.
2. Add frontend Next.js skeleton.
3. Add backend Spring Boot skeleton.
4. Add infra placeholders.
5. Add `docs/technical-design.md` and this `AGENTS.md`.
6. Add basic README with local run instructions.
