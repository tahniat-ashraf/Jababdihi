# Vercel Setup Guide — Jababdihi Frontend

This guide walks through connecting the Jababdihi monorepo to Vercel using
the GitHub integration so that every pull request gets a preview deployment
and every merge to `main` triggers a production deployment.

No Vercel tokens or CI credentials are needed — Vercel manages deployments
automatically through the GitHub app.

---

## 1. Log in to Vercel

Open [vercel.com](https://vercel.com) and sign in (or create an account).
Use the same GitHub account that owns the `tahniat-ashraf/Jababdihi` repository.

---

## 2. Import the GitHub repository

1. From the Vercel dashboard, click **Add New → Project**.
2. Select **Continue with GitHub** and authorise Vercel if prompted.
3. Find and select:

   ```
   tahniat-ashraf/jababdihi
   ```

4. Click **Import**.

---

## 3. Set the project root directory

Because the Next.js app lives inside the `frontend/` subdirectory of the
monorepo, Vercel must be told where to find it.

On the **Configure Project** screen:

1. Expand **Root Directory**.
2. Enter:

   ```
   frontend
   ```

3. Click **Continue**.

> **Why:** Without this, Vercel looks for `package.json` at the repo root and
> fails to detect the framework.

---

## 4. Confirm the framework preset

Vercel should auto-detect **Next.js** once the root directory is set.
Verify that the **Framework Preset** dropdown shows **Next.js** before
proceeding.

No `vercel.json` file is required — Next.js is detected automatically.

---

## 5. Configure environment variables

Click **Environment Variables** and add the following.

### Server-side variables (not exposed to the browser)

| Name | Preview value | Production value |
|---|---|---|
| `BACKEND_API_BASE_URL` | `https://staging-api.<domain>` | `https://api.<domain>` |
| `ADMIN_API_KEY` | *(your staging admin API key)* | *(your production admin API key)* |
| `ADMIN_USERNAME` | *(your admin username)* | *(your admin username)* |
| `ADMIN_PASSWORD` | *(your admin password)* | *(your admin password)* |

### Public variable (exposed to the browser bundle)

| Name | Preview value | Production value |
|---|---|---|
| `NEXT_PUBLIC_API_BASE_URL` | `https://staging-api.<domain>` | `https://api.<domain>` |

> **Notes:**
> - `BACKEND_API_BASE_URL` takes precedence over `NEXT_PUBLIC_API_BASE_URL`
>   in server-side code (API route handlers and server components). Set both
>   to the same backend URL to keep behaviour consistent.
> - `ADMIN_API_KEY` must match `APP_ADMIN_API_KEY` on the Spring Boot backend.
> - `ADMIN_USERNAME` / `ADMIN_PASSWORD` are used by the Next.js Edge middleware
>   that guards all routes under `/admin`.
> - Replace `<domain>` with your actual domain, for example `jababdihi.com`.

---

## 6. Confirm the production branch

Under **Git** settings (or on the same screen), verify:

```
Production Branch: main
```

Merging a pull request into `main` triggers a **Production Deployment**.
All other branches and PRs trigger **Preview Deployments**.

---

## 7. Confirm Preview Deployments are enabled

In **Project Settings → Git**, confirm:

- **Preview Deployments** is enabled for pull requests and branches.

Each opened or updated PR will receive a unique preview URL posted as a
GitHub deployment status check.

---

## 8. Optional — Enable Vercel Deployment Protection for previews

To prevent unauthenticated access to preview URLs (recommended before public
launch):

1. Go to **Project Settings → Deployment Protection**.
2. Enable **Vercel Authentication** for Preview deployments.

Visitors must then log in with a Vercel account to view preview deployments.
Production deployments are publicly accessible regardless of this setting.

---

## 9. After setup — verify the full flow

### Open a PR and check the preview

1. Create a branch, make a small change, and open a pull request against `main`.
2. Wait for the **Vercel** deployment check to appear on the PR.
3. Click the **Visit** link and confirm the preview frontend loads.
4. Confirm the preview frontend is calling the **staging** backend
   (`staging-api.<domain>`) and not `localhost`.
   - Open browser DevTools → Network and look at the `/api/incidents` request.

### Merge to main and check production

1. Merge the PR into `main`.
2. In the Vercel dashboard, confirm a **Production Deployment** is created.
3. Open the production URL and confirm it loads and calls `api.<domain>`.

---

## Troubleshooting

### Missing environment variables — feed shows an error or blank state

**Symptom:** The feed fails to load; browser DevTools shows a 502 or network
error on `/api/incidents`.

**Cause:** `NEXT_PUBLIC_API_BASE_URL` (and/or `BACKEND_API_BASE_URL`) is not
set on the Vercel project. The code falls back to `http://127.0.0.1:8080`,
which is unreachable from Vercel's servers.

**Fix:** Add the missing variable under **Project Settings → Environment
Variables** and **redeploy** (Vercel does not automatically redeploy when you
add a variable; trigger a new deployment manually or push a new commit).

---

### Wrong root directory — build fails immediately

**Symptom:** Vercel build log shows `package.json not found` or detects the
wrong framework.

**Cause:** The root directory is set to `/` (the repo root) instead of
`frontend`.

**Fix:** Go to **Project Settings → General → Root Directory** and change it
to `frontend`, then redeploy.

---

### Frontend calling localhost in production

**Symptom:** The deployed site loads but all API requests fail. DevTools shows
requests going to `http://127.0.0.1:8080` or `http://localhost:8080`.

**Cause:** Neither `BACKEND_API_BASE_URL` nor `NEXT_PUBLIC_API_BASE_URL` is
set, so the fallback default kicks in.

**Fix:** Same as above — set the env vars and redeploy.

---

### CORS error against the staging backend

**Symptom:** Browser DevTools shows a CORS error on API requests from the
preview URL to the staging backend.

**Cause:** The Spring Boot backend's CORS configuration does not include the
Vercel preview domain.

**Fix:** Add the Vercel deployment domain (e.g.
`https://jababdihi-*.vercel.app`) to the `allowed-origins` list in the
backend's `application-staging.yml`. Wildcards in origin patterns are
supported by Spring's `CorsConfiguration`.

---

### Production accidentally pointing to the staging API

**Symptom:** Production data is missing or shows only seed data; logs show
requests going to `staging-api.<domain>`.

**Cause:** The environment variables were set for **all environments** instead
of being scoped to **Preview** and **Production** separately.

**Fix:**
1. In **Project Settings → Environment Variables**, delete the variable.
2. Re-add it twice — once scoped to **Preview** with the staging URL, and
   once scoped to **Production** with the production URL.
3. Redeploy both environments.
