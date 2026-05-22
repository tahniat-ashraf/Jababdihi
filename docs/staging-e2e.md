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
2. Resolves the frontend URL.
   - Pull requests use the deterministic Vercel branch preview URL.
   - Pushes to `main` use `STAGING_FRONTEND_URL` if configured.
3. Sets up Node.js and installs Playwright on the runner when a frontend URL is available.
4. Runs `npm run test:e2e:staging` against the resolved frontend URL.
5. Falls back to backend-only API smoke tests on `main` when `STAGING_FRONTEND_URL` is not configured.
6. Uploads the Playwright report as a GitHub Actions artifact if tests fail.

Pull-request E2E requires `VERCEL_TEAM_SLUG`. Full frontend E2E on pushes to
`main` additionally requires `STAGING_FRONTEND_URL`; otherwise the workflow
still verifies the staging backend.

---

## Setup

### 1. GitHub repository variables

Set the required Vercel team slug at **Settings → Secrets and variables →
Actions → Variables**:

| Variable | Value | Notes |
|----------|-------|-------|
| `VERCEL_TEAM_SLUG` | `ta-workspace` | The slug that appears in your Vercel preview URLs, e.g. `jababdihi-git-main-**ta-workspace**.vercel.app` |

For pull requests, the CI job derives the frontend URL automatically from the
branch name using Vercel's deterministic branch-alias format:

```
https://jababdihi-git-{branch-slug}-{VERCEL_TEAM_SLUG}.vercel.app
```

For branch `feat/my-feature` → `jababdihi-git-feat-my-feature-ta-workspace.vercel.app`.
This URL always points to the **latest** Vercel deployment of that branch —
no hash, stable for the lifetime of the branch.

For pushes to `main`, add optional `STAGING_FRONTEND_URL` if you want full
Playwright coverage against a stable staging frontend domain. Without it, CI
runs backend-only smoke tests.

`STAGING_API_BASE_URL` is derived from `STAGING_VPS_HOST` and does not need a
separate variable.

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

# 2. Derive the branch alias URL (or look it up in the Vercel dashboard)
#    Format: https://jababdihi-git-{branch-slug}-ta-workspace.vercel.app
#    e.g. for branch feat/my-feature:
#    https://jababdihi-git-feat-my-feature-ta-workspace.vercel.app

# 3. Run the tests
cd frontend
STAGING_FRONTEND_URL=https://jababdihi-git-<branch-slug>-ta-workspace.vercel.app \
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

**Main-branch run only performs backend smoke tests:**
Set optional `STAGING_FRONTEND_URL` under **Settings → Secrets and variables →
Actions → Variables** (not Secrets) to enable full Playwright coverage on
pushes to `main`.

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
