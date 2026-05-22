# Deployment Readiness Checklist

Work through this checklist in order before treating any environment as
production-ready. Each section lists what must be true, not just what must
be clicked. Tick boxes as you go.

---

## 1. Domains

Decide on your domain names first — every other section references them.

| URL | Purpose | Example |
|-----|---------|---------|
| **Staging frontend** | Vercel branch-alias preview | `https://jababdihi-git-main-ta-workspace.vercel.app` or `https://staging.jababdihi.com` |
| **Production frontend** | Vercel production deployment | `https://jababdihi.com` |
| **Staging API** | VPS backend behind NGINX, port 8081 | `https://staging-api.jababdihi.com` |
| **Production API** | VPS backend behind NGINX, port 80/443 | `https://api.jababdihi.com` |

Write your chosen values here before continuing:

```
STAGING_FRONTEND_URL  = ______________________________________
PRODUCTION_FRONTEND_URL = ____________________________________
STAGING_API_URL       = ______________________________________
PRODUCTION_API_URL    = ______________________________________
```

---

## 2. DNS (Cloudflare)

Assuming Cloudflare as the DNS provider. All records should have **Proxy status: DNS only (grey cloud)** for the VPS A records so Cloudflare doesn't intercept the SSH port.

### 2.1 Vercel frontend records

Vercel provides the values — go to **Vercel → Project → Settings → Domains** and
copy the CNAME / A record targets it gives you.

- [ ] `jababdihi.com` → A record (or CNAME to `cname.vercel-dns.com`) — **Production**
- [ ] `www.jababdihi.com` → CNAME to `cname.vercel-dns.com` — **Production alias**
- [ ] `staging.jababdihi.com` → CNAME to `cname.vercel-dns.com` — **Staging frontend** *(optional; Vercel branch alias works without a custom domain)*

### 2.2 VPS API records

| Record | Type | Value | Notes |
|--------|------|-------|-------|
| `staging-api.<domain>` | A | `<STAGING_VPS_IP>` | Proxy: DNS only |
| `api.<domain>` | A | `<PROD_VPS_IP>` | Proxy: DNS only |

- [ ] Staging A record created and propagating (`dig staging-api.<domain>` returns the VPS IP)
- [ ] Production A record created and propagating

### 2.3 DNS propagation check

```bash
dig +short staging-api.<YOUR_DOMAIN>   # must return staging VPS IP
dig +short api.<YOUR_DOMAIN>           # must return production VPS IP
```

- [ ] Both resolve to the correct IPs

---

## 3. Vercel

### 3.1 Project settings

- [ ] **Root Directory** is set to `frontend` (Settings → General → Root Directory)
- [ ] **Framework preset** is set to Next.js
- [ ] **Node.js version** matches the project (≥ 20)
- [ ] Production domain `jababdihi.com` (or equivalent) is linked in Settings → Domains

### 3.2 Preview environment variables

Set at **Settings → Environment Variables**, scope: **Preview**.

| Variable | Value | Sensitive |
|----------|-------|-----------|
| `BACKEND_API_BASE_URL` | `http://<STAGING_VPS_IP>:8081` → later `https://staging-api.<domain>` once cert is up | No |
| `ADMIN_USERNAME` | Admin username | Yes |
| `ADMIN_PASSWORD` | Admin password (plain — compared by Next.js middleware) | Yes |

- [ ] `BACKEND_API_BASE_URL` set for Preview
- [ ] `ADMIN_USERNAME` set for Preview
- [ ] `ADMIN_PASSWORD` set for Preview

### 3.3 Production environment variables

Set at **Settings → Environment Variables**, scope: **Production**.

| Variable | Value | Sensitive |
|----------|-------|-----------|
| `BACKEND_API_BASE_URL` | `https://api.<YOUR_DOMAIN>` | No |
| `ADMIN_USERNAME` | Admin username | Yes |
| `ADMIN_PASSWORD` | Admin password | Yes |

- [ ] `BACKEND_API_BASE_URL` set for Production
- [ ] `ADMIN_USERNAME` set for Production
- [ ] `ADMIN_PASSWORD` set for Production

### 3.4 Deployment protection (recommended)

Prevent crawlers and the public from discovering the admin page on preview URLs.

- [ ] **Settings → Deployment Protection → Vercel Authentication** is enabled for Preview deployments
  *(Vercel Hobby allows this; it forces a Vercel login to view any preview URL)*

---

## 4. GitHub

### 4.1 Repository secrets

Set at **Settings → Secrets and variables → Actions → Secrets**.

**Staging VPS**

| Secret | Value |
|--------|-------|
| `STAGING_VPS_HOST` | Staging VPS IP |
| `STAGING_VPS_PORT` | `22` |
| `STAGING_VPS_USER` | `deploy` |
| `STAGING_VPS_SSH_KEY` | Contents of `~/.ssh/jababdihi_deploy_staging` (no passphrase) |
| `STAGING_DEPLOY_PATH` | `/opt/jababdihi/staging/infra` |

- [ ] All 5 staging secrets present

**Production VPS**

| Secret | Value |
|--------|-------|
| `PROD_VPS_HOST` | Production VPS IP |
| `PROD_VPS_PORT` | `22` |
| `PROD_VPS_USER` | `deploy` |
| `PROD_VPS_SSH_KEY` | Contents of `~/.ssh/jababdihi_deploy_prod` (no passphrase) |
| `PROD_DEPLOY_PATH` | `/opt/jababdihi/prod/infra` |

- [ ] All 5 production secrets present

**Container registry**

| Secret | Value |
|--------|-------|
| `GHCR_TOKEN` | Fine-grained PAT with `read:packages` + `write:packages` scoped to this repo |

- [ ] `GHCR_TOKEN` present

### 4.2 Repository variables

Set at **Settings → Secrets and variables → Actions → Variables**.

| Variable | Value |
|----------|-------|
| `VERCEL_TEAM_SLUG` | `ta-workspace` (the slug in your Vercel preview URLs) |

- [ ] `VERCEL_TEAM_SLUG` present

### 4.3 Environments

Create at **Settings → Environments**.

**`staging`**

- [ ] Environment `staging` exists
- [ ] No approval required
- [ ] No deployment branch restriction (or set to `main` + PRs)

**`production`**

- [ ] Environment `production` exists (lowercase — workflow uses `production`)
- [ ] **Required reviewers** — add yourself (and any co-maintainers)
- [ ] **Allowed branches** restricted to `main` only
- [ ] The old `production-migrations` environment is **deleted** (no longer used)

### 4.4 Branch protection on `main`

**Settings → Branches → Add branch protection rule → `main`**

- [ ] **Require a pull request before merging** — enabled
- [ ] **Require status checks to pass** — add:
  - `Build and test` (from `backend-ci.yml`)
  - `Build backend image` (from `backend-deploy.yml`)
- [ ] **Do not allow bypassing the above settings** — enabled

---

## 5. VPS — per environment

Run the same checklist for both staging and production VPS. In the commands
below, substitute `staging` / `prod` as appropriate.

### 5.1 System packages

```bash
# On the VPS
docker --version          # must be 24+
docker compose version    # must be v2.1+
rsync --version
curl --version
```

- [ ] Docker 24+ installed
- [ ] Docker Compose plugin v2.1+ installed (`docker compose`, not `docker-compose`)
- [ ] `rsync` installed
- [ ] `curl` installed

### 5.2 Deploy user

```bash
id deploy                          # user exists
sudo -l -U deploy                  # check sudo rights if needed
cat ~/.ssh/authorized_keys         # CI public key is listed
```

- [ ] `deploy` user exists
- [ ] CI public key (`jababdihi_deploy_staging.pub` / `jababdihi_deploy_prod.pub`) is in `deploy`'s `~/.ssh/authorized_keys`
- [ ] `PasswordAuthentication no` in `/etc/ssh/sshd_config`
- [ ] `PermitRootLogin no` in `/etc/ssh/sshd_config`

### 5.3 Firewall (UFW + Hostinger panel)

Verify with `sudo ufw status`:

| Port | Protocol | Rule |
|------|----------|------|
| 22 | TCP | ALLOW (SSH) |
| 80 | TCP | ALLOW (HTTP / production NGINX) |
| 443 | TCP | ALLOW (HTTPS) |
| 8081 | TCP | ALLOW (staging NGINX — **staging VPS only**) |

And in the **Hostinger panel → Firewall**: the same ports must be listed as Accept with status **Synchronized**.

- [ ] UFW active with correct rules
- [ ] Hostinger panel rules match and are **Synchronized**

### 5.4 Repository clone

```bash
ls /opt/jababdihi/<env>/infra/docker-compose.base.yml
ls /opt/jababdihi/<env>/infra/scripts/deploy-staging.sh   # or deploy-prod.sh
```

- [ ] Repo cloned to `/opt/jababdihi/<env>/`
- [ ] Scripts are executable (`chmod +x infra/scripts/*.sh`)

### 5.5 GHCR login (for manual pulls)

The deploy workflow logs in automatically, but the VPS also needs to be able
to pull images for manual recovery:

```bash
echo "<GHCR_TOKEN>" | docker login ghcr.io -u tahniat-ashraf --password-stdin
```

- [ ] `docker login ghcr.io` succeeds on the VPS
- [ ] Login credentials persisted in `~/.docker/config.json` for the `deploy` user

### 5.6 Environment file

```bash
ls -la /opt/jababdihi/<env>/infra/.env.<env>
```

- [ ] `.env.staging` present (copied from `.env.staging.example`, all `<REPLACE_*>` values filled in)
- [ ] `.env.prod` present (copied from `.env.prod.example`, all `<REPLACE_*>` values filled in)
- [ ] `BACKEND_IMAGE` line has been updated from the placeholder to a real image tag (the deploy script updates this automatically on each run — just ensure it's not `<IMAGE_TAG>` for the first deploy)
- [ ] `POSTGRES_PASSWORD` is a strong random password (not the placeholder)
- [ ] `APP_ADMIN_API_KEY` is a strong random key
- [ ] `CORS_ALLOWED_ORIGINS` matches the frontend domain(s)
- [ ] `SERVER_NAME` matches the API subdomain (`staging-api.<domain>` / `api.<domain>`)

### 5.7 Docker volumes (first deploy only)

Docker Compose creates named volumes automatically on `up`. No manual action
needed — but confirm after the first deploy:

```bash
docker volume ls | grep jababdihi
```

Expected volumes:

| Volume | Environment |
|--------|-------------|
| `jababdihi_staging_postgres_data` | staging |
| `jababdihi_staging_redis_data` | staging |
| `jababdihi_prod_postgres_data` | production |
| `jababdihi_prod_redis_data` | production |

- [ ] Volumes present after first deploy

### 5.8 NGINX config

The NGINX config template is in `infra/nginx/default.conf.template` and is
mounted into the container by Docker Compose. No manual NGINX installation
needed — NGINX runs in a container.

- [ ] `infra/nginx/default.conf.template` is present in the repo clone on the VPS
- [ ] `SERVER_NAME` in `.env.<env>` matches the domain NGINX should serve

### 5.9 TLS / HTTPS (post-DNS)

Once the DNS A record resolves correctly, issue a Let's Encrypt certificate.
NGINX in the container handles HTTP; terminate TLS at the host level using
Certbot + a host-level reverse proxy, or configure NGINX in the container to
use the cert directly.

*This step is manual and environment-specific. Document your approach here.*

- [ ] TLS strategy chosen (Certbot on host / Cloudflare Tunnel / other)
- [ ] Certificate issued and renewed automatically
- [ ] `CORS_ALLOWED_ORIGINS` and `SERVER_NAME` updated to use `https://` after cert is live
- [ ] `.env.<env>` updated and services restarted

---

## 6. Validation

Run these checks in order after completing sections 1–5. All must pass before
going live.

### 6.1 Staging backend health

```bash
curl -s http://<STAGING_VPS_IP>:8081/actuator/health | python3 -m json.tool
```

Expected:

```json
{ "status": "UP" }
```

- [ ] Returns `{"status":"UP"}`
- [ ] All five containers running: `nginx`, `backend-api`, `backend-worker`, `postgres`, `redis`

```bash
ssh deploy@<STAGING_VPS_IP>
docker compose --env-file /opt/jababdihi/staging/infra/.env.staging \
  -f /opt/jababdihi/staging/infra/docker-compose.base.yml \
  -f /opt/jababdihi/staging/infra/docker-compose.staging.yml \
  ps
```

- [ ] All five services show `(healthy)` or `Up`

### 6.2 Staging frontend loads

Open `STAGING_FRONTEND_URL` in a browser:

- [ ] Page loads without "Public API is unavailable"
- [ ] Incident feed shows cards (or "no incidents" message — not a 502)
- [ ] Browser DevTools → Network shows `/api/incidents` returning 200, not 502
- [ ] No CORS errors in the Console tab

### 6.3 Staging E2E smoke tests pass

```bash
# From your local machine
cd frontend
STAGING_FRONTEND_URL=<your-staging-frontend-url> \
STAGING_API_BASE_URL=http://<STAGING_VPS_IP>:8081 \
npm run test:e2e:staging
```

- [ ] All 10 Playwright tests pass (see `docs/staging-e2e.md`)

### 6.4 Production backend health *(when prod VPS is set up)*

```bash
curl -s https://api.<YOUR_DOMAIN>/actuator/health | python3 -m json.tool
```

- [ ] Returns `{"status":"UP"}`

### 6.5 Production frontend loads *(when prod VPS is set up)*

- [ ] `https://jababdihi.com` (or your prod URL) loads the feed
- [ ] No CORS errors
- [ ] No "Public API is unavailable"

### 6.6 GitHub Actions CI passes on a test PR

Open a small PR touching `backend/` or `infra/` and verify:

- [ ] `Build and test` job passes (spotless + tests + package)
- [ ] `Build backend image` job passes
- [ ] `Deploy staging` job passes (ends with "Staging is healthy")
- [ ] `Staging E2E smoke tests` job passes (all 10 Playwright tests)
- [ ] Merge → `Deploy production` job pauses for approval
- [ ] After approval → production deploys and health check passes

---

## 7. Quick reference — current environment status

| Item | Staging | Production |
|------|---------|------------|
| VPS IP | `YOUR_STAGING_VPS_IP` | *(not set up)* |
| NGINX port | `8081` | `80` |
| API domain | *(pending DNS)* | *(pending DNS)* |
| GitHub secrets | ✅ set | ❌ not set |
| `.env` file on VPS | ✅ present | ❌ not present |
| Containers healthy | ✅ | ❌ |
| TLS | ❌ pending | ❌ pending |
| GitHub environment | ✅ `staging` | ✅ `production` |
| E2E passing in CI | ✅ (when `VERCEL_TEAM_SLUG` set) | n/a |
