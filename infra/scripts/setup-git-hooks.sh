#!/usr/bin/env sh
# Sets up local git hooks for the Jababdihi repository.
#
# Run once after cloning:
#   ./infra/scripts/setup-git-hooks.sh
#
# Hooks installed:
#   pre-commit  — scans staged changes for secrets (gitleaks).
#   pre-push    — two checks on every push:
#                   1. Secret scan on all commits being pushed.
#                   2. When pushing to 'production', blocks the push if the
#                      staging backend is not healthy.
#
# ── Deployment workflow ───────────────────────────────────────────────────────
#
#   1. Merge PR to main  →  CI auto-deploys to staging + runs E2E.
#   2. When you are happy with staging:
#
#        git push origin main:production
#
#   3. Pre-push hook fires:
#        a. Secret scan on outgoing commits.
#        b. Checks staging health  →  blocks if staging is down.
#   4. GitHub receives the push, waits for manual approval.
#   5. Approve in Actions  →  production deploys + release is created.
#
# ── One-time staging URL setup ────────────────────────────────────────────────
#
# The production gate needs to know your staging API URL.
# Run once — stored in .git/config, never committed:
#
#   git config deploy.staging-api-url "http://<YOUR_STAGING_VPS_IP>:8081"
#
# ─────────────────────────────────────────────────────────────────────────────
set -eu

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOK_DIR="$(git rev-parse --git-dir)/hooks"

# ── pre-commit ────────────────────────────────────────────────────────────────
cat > "$HOOK_DIR/pre-commit" <<'HOOK'
#!/usr/bin/env sh
# Scan staged changes for secrets before every commit.
if ! command -v gitleaks > /dev/null 2>&1; then
  echo "[pre-commit] WARNING: gitleaks not found — secret scan skipped."
  echo "  brew install gitleaks  |  https://github.com/gitleaks/gitleaks/releases"
  exit 0
fi
REPO_ROOT="$(git rev-parse --show-toplevel)"
CONFIG="$REPO_ROOT/.gitleaks.toml"
if [ -f "$CONFIG" ]; then
  gitleaks protect --staged --config "$CONFIG" --redact --verbose
else
  gitleaks protect --staged --redact --verbose
fi
HOOK
chmod +x "$HOOK_DIR/pre-commit"
echo "[setup] Installed pre-commit hook  → $HOOK_DIR/pre-commit"

# ── pre-push ──────────────────────────────────────────────────────────────────
cat > "$HOOK_DIR/pre-push" <<'HOOK'
#!/usr/bin/env sh
# Two checks on every push:
#   1. Scan outgoing commits for secrets (gitleaks).
#   2. When pushing to 'production', verify staging is healthy first.
#
# Git passes the list of refs being pushed on stdin (one per line):
#   <local-ref> <local-sha> <remote-ref> <remote-sha>
# stdin can only be read once, so buffer it immediately.

REPO_ROOT="$(git rev-parse --show-toplevel)"
CONFIG="$REPO_ROOT/.gitleaks.toml"

# Buffer all of stdin so we can iterate over it multiple times.
PUSH_REFS=$(cat)

PUSHING_TO_PRODUCTION=0
SCAN_RANGES=""

while read -r local_ref local_sha remote_ref remote_sha; do
  # Skip deletions (local_sha = all-zeros means the ref is being deleted).
  [ "$local_sha" = "0000000000000000000000000000000000000000" ] && continue

  if [ "$remote_ref" = "refs/heads/production" ]; then
    PUSHING_TO_PRODUCTION=1
  fi

  # Build the range of new commits for the secret scan.
  if [ "$remote_sha" = "0000000000000000000000000000000000000000" ]; then
    SCAN_RANGES="$SCAN_RANGES $local_sha"
  else
    SCAN_RANGES="$SCAN_RANGES $remote_sha..$local_sha"
  fi
done <<EOF
$PUSH_REFS
EOF

# ── 1. Secret scan ────────────────────────────────────────────────────────────
if command -v gitleaks > /dev/null 2>&1; then
  for range in $SCAN_RANGES; do
    if [ -f "$CONFIG" ]; then
      gitleaks detect --config "$CONFIG" --log-opts "$range" --redact --verbose || exit 1
    else
      gitleaks detect --log-opts "$range" --redact --verbose || exit 1
    fi
  done
else
  echo "[pre-push] WARNING: gitleaks not found — secret scan skipped."
  echo "  brew install gitleaks  |  https://github.com/gitleaks/gitleaks/releases"
fi

# ── 2. Production gate ────────────────────────────────────────────────────────
if [ "$PUSHING_TO_PRODUCTION" -eq 1 ]; then
  echo ""
  echo "[pre-push] Pushing to production — checking staging health first..."

  STAGING_URL=$(git config deploy.staging-api-url 2>/dev/null || echo "")

  if [ -z "$STAGING_URL" ]; then
    echo ""
    echo "[pre-push] WARNING: deploy.staging-api-url is not set."
    echo "  Staging health check skipped. Configure it once with:"
    echo "    git config deploy.staging-api-url http://<STAGING_VPS_IP>:8081"
    echo ""
    echo "  GitHub will still require manual approval before production deploys."
    echo ""
  else
    RESPONSE=$(curl -fsS --max-time 10 "$STAGING_URL/actuator/health" 2>/dev/null || echo "")
    if echo "$RESPONSE" | grep -q '"UP"'; then
      echo "[pre-push] ✓ Staging is healthy."
      echo ""
    else
      echo ""
      echo "[pre-push] ✗ BLOCKED: staging is not healthy."
      echo "  URL checked: $STAGING_URL/actuator/health"
      echo "  Response:    $RESPONSE"
      echo ""
      echo "  Fix staging before promoting to production, then re-run:"
      echo "    git push origin main:production"
      echo ""
      exit 1
    fi
  fi
fi
HOOK
chmod +x "$HOOK_DIR/pre-push"
echo "[setup] Installed pre-push hook    → $HOOK_DIR/pre-push"

echo ""
echo "Git hooks installed successfully."
echo ""
echo "One-time setup — tell the hook where staging lives:"
echo "  git config deploy.staging-api-url http://<YOUR_STAGING_VPS_IP>:8081"
echo ""
echo "Deployment workflow:"
echo "  1. git push origin main          # CI: staging deploy + E2E"
echo "  2. git push origin main:production  # hook checks staging, then CI asks for approval"
echo "  3. Approve in GitHub Actions     # production deploys + release created"
