# GitHub Actions Secrets

Required secrets for the backend CI/CD workflows.
Set these at **Settings → Secrets and variables → Actions** in the GitHub repository.

---

## VPS connection secrets

These are used by the deploy workflow to SSH into the VPS and run the deploy scripts.
Staging and production use separate credentials so a staging key compromise cannot
affect production.

| Secret | Description |
|--------|-------------|
| `STAGING_VPS_HOST` | IP address or hostname of the staging VPS |
| `STAGING_VPS_PORT` | SSH port (usually `22`) |
| `STAGING_VPS_USER` | SSH username (e.g. `deploy`) |
| `STAGING_VPS_SSH_KEY` | Full contents of the staging deploy private key (`~/.ssh/jababdihi_deploy`) |
| `STAGING_DEPLOY_PATH` | Absolute path to the infra directory on the staging VPS (e.g. `/opt/jababdihi/staging/repo/infra`) |
| `PROD_VPS_HOST` | IP address or hostname of the production VPS |
| `PROD_VPS_PORT` | SSH port (usually `22`) |
| `PROD_VPS_USER` | SSH username (e.g. `deploy`) |
| `PROD_VPS_SSH_KEY` | Full contents of the production deploy private key |
| `PROD_DEPLOY_PATH` | Absolute path to the infra directory on the production VPS (e.g. `/opt/jababdihi/prod/repo/infra`) |

---

## Container registry

| Secret | Description |
|--------|-------------|
| `GHCR_TOKEN` | GitHub Personal Access Token with `write:packages` (for pushing images in CI) and `read:packages` (for pulling on VPS). Scope to the repository. |

---

## Domain references

These are used by the deploy workflow to verify the deployed endpoint and
to construct CORS or redirect values.

| Secret | Description |
|--------|-------------|
| `STAGING_DOMAIN` | Public domain for the staging API (e.g. `staging-api.example.com`) |
| `PROD_DOMAIN` | Public domain for the production API (e.g. `api.example.com`) |

---

## GitHub Environments

The backend deploy workflow uses GitHub Environments to gate approvals.
Create these environments at **Settings → Environments**:

| Environment | Purpose |
|-------------|---------|
| `staging` | Staging deploy. No approval required. |
| `production` | Production deploy. Require at least one reviewer. Flyway migrations and service rollout run as a single approved step via `deploy-prod.sh`. |

Require branch protection on `main` (Settings → Branches) so the production
environment is only reachable from merged commits.

### Approving a production deploy

1. A push to `main` triggers the workflow. The `test` and `build-image` jobs run automatically.
2. Once the image is built, the `deploy-production` job pauses and shows **"Waiting for review"** in the Actions UI.
3. A reviewer visits the run, clicks **Review deployments**, selects `production`, and clicks **Approve and deploy**.
4. The workflow SSHes into the production VPS and runs `deploy-prod.sh`, which:
   - Pulls the new image
   - Runs Flyway migrations in a one-shot container
   - Starts all services with `docker compose up -d --wait`
   - Polls `/actuator/health` for up to 90 s
5. If health check passes, the job turns green. If it fails, the job exits non-zero and services remain on the previous image (compose keeps the last healthy container running until explicitly replaced).

---

## .env files stay on the VPS, not in GitHub

Real `.env` files with database passwords, API keys, and other secrets live on the
VPS at `/opt/jababdihi/<env>/repo/infra/.env.<env>`.

They are never committed to the repository and are not injected via GitHub secrets.

This keeps production secrets off GitHub entirely. The deploy scripts read the env
file that is already present on the VPS.

If you later want secrets managed differently (e.g. injected from GitHub secrets
into the VPS env file during CI), the deploy workflow can write the file before
calling the deploy script. Document that decision here if you adopt it.

---

## Generating the SSH deploy key

```bash
# On your local machine
ssh-keygen -t ed25519 -C "jababdihi-deploy-staging" -f ~/.ssh/jababdihi_deploy_staging
ssh-keygen -t ed25519 -C "jababdihi-deploy-prod" -f ~/.ssh/jababdihi_deploy_prod
```

Add the **public key** (`.pub` file contents) to `~/.ssh/authorized_keys` on the VPS
(see [hostinger-vps-setup.md](hostinger-vps-setup.md), step 11).

Add the **private key** (no `.pub` extension, full file contents) to the GitHub secret
`STAGING_VPS_SSH_KEY` or `PROD_VPS_SSH_KEY`.

---

## Generating the GHCR token

1. Go to GitHub → Settings → Developer settings → Personal access tokens → Fine-grained tokens.
2. Create a token scoped to the `jababdihi` repository.
3. Grant **Read and Write** for Packages.
4. Add the token value as the `GHCR_TOKEN` secret.
