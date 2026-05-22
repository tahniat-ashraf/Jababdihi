#!/usr/bin/env sh
# Sets up local git hooks for the Jababdihi repository.
#
# Run once after cloning:
#   ./infra/scripts/setup-git-hooks.sh
#
# What it installs:
#   pre-commit  — scans staged changes with gitleaks before every commit.
#   pre-push    — scans the full range of commits being pushed.
#
# Both hooks are non-blocking if gitleaks is not installed (a warning is
# printed instead), because the CI secret-scan job acts as the hard gate.
# Installing gitleaks locally gives you faster feedback.
#
# Install gitleaks:
#   macOS:  brew install gitleaks
#   Linux:  https://github.com/gitleaks/gitleaks/releases
set -eu

REPO_ROOT="$(git rev-parse --show-toplevel)"
HOOK_DIR="$(git rev-parse --git-dir)/hooks"

# ── pre-commit hook ───────────────────────────────────────────────────────────
cat > "$HOOK_DIR/pre-commit" <<'HOOK'
#!/usr/bin/env sh
# Pre-commit: scan staged changes for secrets.
if ! command -v gitleaks > /dev/null 2>&1; then
  echo "[pre-commit] WARNING: gitleaks not found — secret scan skipped."
  echo "  Install: brew install gitleaks  (or see https://github.com/gitleaks/gitleaks/releases)"
  echo "  The CI secret-scan job will still catch secrets before they reach GitHub."
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

# ── pre-push hook ─────────────────────────────────────────────────────────────
cat > "$HOOK_DIR/pre-push" <<'HOOK'
#!/usr/bin/env sh
# Pre-push: scan all commits being pushed that aren't already on the remote.
if ! command -v gitleaks > /dev/null 2>&1; then
  echo "[pre-push] WARNING: gitleaks not found — secret scan skipped."
  echo "  Install: brew install gitleaks  (or see https://github.com/gitleaks/gitleaks/releases)"
  exit 0
fi

REPO_ROOT="$(git rev-parse --show-toplevel)"
CONFIG="$REPO_ROOT/.gitleaks.toml"

# Read the list of refs being pushed from stdin (format: <local-ref> <local-sha> <remote-ref> <remote-sha>)
while read -r local_ref local_sha remote_ref remote_sha; do
  # Skip branch deletions
  if [ "$local_sha" = "0000000000000000000000000000000000000000" ]; then
    continue
  fi

  # If remote doesn't have the branch yet, scan from the beginning of local history
  if [ "$remote_sha" = "0000000000000000000000000000000000000000" ]; then
    range="$local_sha"
  else
    range="$remote_sha..$local_sha"
  fi

  if [ -f "$CONFIG" ]; then
    gitleaks detect --config "$CONFIG" --log-opts "$range" --redact --verbose
  else
    gitleaks detect --log-opts "$range" --redact --verbose
  fi
done
HOOK
chmod +x "$HOOK_DIR/pre-push"
echo "[setup] Installed pre-push hook    → $HOOK_DIR/pre-push"

echo ""
echo "Git hooks installed. To verify gitleaks is available:"
echo "  gitleaks version"
echo ""
echo "To install gitleaks if missing:"
echo "  macOS: brew install gitleaks"
echo "  Other: https://github.com/gitleaks/gitleaks/releases"
