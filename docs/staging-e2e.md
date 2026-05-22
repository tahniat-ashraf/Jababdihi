# Staging E2E Smoke Tests

End-to-end smoke tests that run against the **deployed** staging frontend and
staging backend after every staging deploy.

---

## What the tests cover

| # | Test | File |
|---|------|------|
| 1 | Staging backend health (`/actuator/health` returns `UP`) | `smoke.staging.spec.ts` |
| 2 | `/bn` route loads without error | `smoke.staging.spec.ts` |
| 3 | Government feed loads at least one incident card | `smoke.staging.spec.ts` |
| 4 | Switching to Opposition updates URL and loads Opposition cards | `smoke.staging.spec.ts` |
| 5 | `/en` shows English actor labels and incident cards | `smoke.staging.spec.ts` |
| 6 | Source chips (including "+N more") render | `smoke.staging.spec.ts` |
| 7 | Confidence gauge shows a numeric score, not `--` | `smoke.staging.spec.ts` |
| 8 | Infinite scroll loads the next page (forced via `pageSize=3`) | `smoke.staging.spec.ts` |
| 9 | Source anchors have `target=_blank` and `rel="noopener noreferrer"` | `smoke.staging.spec.ts` |
| 10 | `PENDING_REVIEW` incidents do not appear in the public feed | `smoke.staging.spec.ts` |

---

## How CI runs these tests

After the `deploy-staging` job succeeds, the `e2e-staging` job:

1. SSHes into the staging VPS and runs `./scripts/run-seed.sh staging`
   (idempotent — skips if seed data is already present).
2. Sets up Node.js and installs Playwright on the runner.
3. Runs `npm run test:e2e:staging` against `STAGING_FRONTEND_URL`.
4. Uploads the Playwright report as a GitHub Actions artifact if tests fail.

The job is skipped if the `STAGING_FRONTEND_URL` repository variable is not set
(see [Setup](#setup) below).

---

## Setup

### 1. GitHub repository variable

Set `STAGING_FRONTEND_URL` at **Settings → Secrets and variables → Actions →
Variables** (not Secrets — it is not sensitive):

| Variable | Example value |
|----------|---------------|
| `STAGING_FRONTEND_URL` | `https://jababdihi-<hash>-ta-workspace.vercel.app` |

**For a fixed staging URL:** if you have a Vercel alias or custom staging domain
(e.g. `staging.jababdihi.com`) use that — it never changes between deployments.

**For PR preview URLs:** Vercel creates a unique URL for each PR commit. Until a
fixed staging alias is configured, set `STAGING_FRONTEND_URL` to the most
recent Vercel preview URL for your main PR. You can find it in the Vercel
dashboard under **Deployments**.

`STAGING_API_BASE_URL` is derived automatically in CI from `STAGING_VPS_HOST`
(`http://<host>:8081`) and does not need a separate variable.

### 2. Staging seed data

The seeder (`StagingDataSeeder.java`) inserts deterministic test data on first
run and skips on subsequent runs. It is activated by `--app.seed=staging` and
is guarded by:

- `@Profile("!prod")` — the bean is never created when the `prod` Spring profile
  is active.
- `@ConditionalOnProperty(name = "app.seed", havingValue = "staging")` — only
  activates when explicitly requested.

---

## Running locally

### Prerequisites

- Staging VPS running with all five containers healthy
- `STAGING_FRONTEND_URL` pointing to the deployed Vercel preview

```bash
# 1. Seed staging data on the VPS (idempotent)
ssh deploy@<STAGING_VPS_IP>
cd /opt/jababdihi/staging/infra
./scripts/run-seed.sh staging
exit

# 2. Run the tests
cd frontend
STAGING_FRONTEND_URL=https://<your-vercel-preview-url> \
STAGING_API_BASE_URL=http://<VPS_IP>:8081 \
npm run test:e2e:staging
```

To run in headed mode for debugging:

```bash
STAGING_FRONTEND_URL=https://... STAGING_API_BASE_URL=http://... \
npx playwright test --config=playwright.staging.config.ts --headed
```

---

## Running seed manually

On the staging VPS (from the `infra/` directory):

```bash
./scripts/run-seed.sh staging
```

The seeder checks for existing seed rows (`source_url LIKE 'https://seed.jababdihi.local/%'`)
and exits immediately if data is already present. It is safe to run repeatedly.

The script **refuses** to run for any other environment:

```
$ ./scripts/run-seed.sh prod
ERROR: run-seed.sh refuses to run against production.
```

---

## Seed data baseline

The seeder inserts 9 incidents:

| Actor | Status | Count |
|-------|--------|-------|
| GOVERNMENT | AUTO_PUBLISHED | 4 |
| GOVERNMENT | MANUALLY_PUBLISHED | 1 |
| GOVERNMENT | PENDING_REVIEW | 1 (must not appear in public feed) |
| OPPOSITION | AUTO_PUBLISHED | 2 |
| OPPOSITION | MANUALLY_PUBLISHED | 1 |

Public feed totals: **5 Government**, **3 Opposition**.

Staging E2E assertions use `≥` rather than `==` so they pass when the staging
database has additional manually-entered incidents beyond the seed baseline.

---

## Troubleshooting

**E2E job skipped silently:**
Check that `STAGING_FRONTEND_URL` is set under **Settings → Secrets and
variables → Actions → Variables** (not Secrets).

**`STAGING_FRONTEND_URL is required` error on local run:**
Export the variable before running the tests:
```bash
export STAGING_FRONTEND_URL=https://...
npm run test:e2e:staging --prefix frontend
```

**Seed step times out or fails:**
Check that the staging backend containers are running and healthy:
```bash
ssh deploy@<VPS_IP>
cd /opt/jababdihi/staging/infra
docker compose --env-file .env.staging -f docker-compose.base.yml -f docker-compose.staging.yml ps
```

**Tests fail with "Public API is unavailable":**
The Vercel preview is not connected to the staging backend. Verify that
`BACKEND_API_BASE_URL` is set in Vercel → Project Settings → Environment
Variables → Preview.
