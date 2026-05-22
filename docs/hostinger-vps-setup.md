# Hostinger VPS Setup

Manual one-time steps to prepare the VPS for Docker Compose deployments.
Run these as `root` on first login; afterwards use the `deploy` user.

---

## 1. SSH into the VPS

```bash
ssh root@<VPS_IP>
```

Replace `<VPS_IP>` with the IP address shown in your Hostinger hPanel.

---

## 2. Create a deploy user

```bash
adduser deploy
usermod -aG sudo deploy
```

---

## 3. Install Docker

```bash
apt-get update
apt-get install -y ca-certificates curl gnupg

install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg \
  | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
chmod a+r /etc/apt/keyrings/docker.gpg

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] \
  https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" \
  | tee /etc/apt/sources.list.d/docker.list > /dev/null

apt-get update
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

---

## 4. Add the deploy user to the docker group

```bash
usermod -aG docker deploy
```

---

## 5. Verify Docker works

```bash
docker run --rm hello-world
```

---

## 6. Create application directories

Each environment gets its own directory containing the `infra/` clone and a `.env` file.

```bash
mkdir -p /opt/jababdihi/staging
mkdir -p /opt/jababdihi/prod
chown -R deploy:deploy /opt/jababdihi
```

---

## 7. Clone the repository on the VPS

```bash
su - deploy
cd /opt/jababdihi/staging
git clone https://github.com/tahniat-ashraf/jababdihi.git repo
cd /opt/jababdihi/prod
git clone https://github.com/tahniat-ashraf/jababdihi.git repo
```

Alternatively, copy only the `infra/` directory if you prefer not to clone the full repo.

---

## 8. Create environment files on the VPS

Staging:

```bash
cp /opt/jababdihi/staging/repo/infra/.env.staging.example \
   /opt/jababdihi/staging/repo/infra/.env.staging
nano /opt/jababdihi/staging/repo/infra/.env.staging
# Fill in all <REPLACE_WITH_...> placeholders
```

Production:

```bash
cp /opt/jababdihi/prod/repo/infra/.env.prod.example \
   /opt/jababdihi/prod/repo/infra/.env.prod
nano /opt/jababdihi/prod/repo/infra/.env.prod
# Fill in all <REPLACE_WITH_...> placeholders
```

**Never commit these files. They contain secrets and stay on the VPS only.**

---

## 9. Verify Compose works

```bash
cd /opt/jababdihi/staging/repo/infra
docker compose --env-file .env.staging \
  -f docker-compose.base.yml \
  -f docker-compose.staging.yml \
  config
```

This should print the merged configuration without errors.

---

## 10. Configure firewall

Allow only SSH, HTTP, and HTTPS. Block everything else.

```bash
ufw default deny incoming
ufw default allow outgoing
ufw allow ssh
ufw allow http
ufw allow https
# Staging NGINX port — only if staging runs on the same VPS as production
ufw allow 8081/tcp
ufw enable
ufw status
```

Do **not** expose PostgreSQL (5432) or Redis (6379) ports. They are bound to the
private Docker network only.

---

## 11. Configure SSH key-based deploy access

This key is used by GitHub Actions to connect and run deploy scripts.

On your local machine (or CI):

```bash
ssh-keygen -t ed25519 -C "jababdihi-deploy" -f ~/.ssh/jababdihi_deploy
```

This creates:
- `~/.ssh/jababdihi_deploy` — private key (add to GitHub secret `STAGING_VPS_SSH_KEY` / `PROD_VPS_SSH_KEY`)
- `~/.ssh/jababdihi_deploy.pub` — public key (add to VPS below)

On the VPS, add the public key to the deploy user:

```bash
su - deploy
mkdir -p ~/.ssh
chmod 700 ~/.ssh
echo "<PASTE_PUBLIC_KEY_CONTENT>" >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
```

Test the connection from your local machine:

```bash
ssh -i ~/.ssh/jababdihi_deploy deploy@<VPS_IP>
```

---

## 12. (Optional) Disable password SSH login

After confirming key-based login works:

```bash
# As root
sed -i 's/^#\?PasswordAuthentication.*/PasswordAuthentication no/' /etc/ssh/sshd_config
sed -i 's/^#\?ChallengeResponseAuthentication.*/ChallengeResponseAuthentication no/' /etc/ssh/sshd_config
systemctl restart sshd
```

---

## 13. Install GitHub Actions runner prerequisites

The deploy workflow SSH-es into the VPS and runs deploy scripts. No runner installation is needed on the VPS — only the SSH access set up in step 11.

---

## 14. GHCR login on the VPS (if using private images)

If the GitHub Container Registry package is private:

```bash
su - deploy
echo "<GHCR_TOKEN>" | docker login ghcr.io -u <GITHUB_USERNAME> --password-stdin
```

The `<GHCR_TOKEN>` is a GitHub Personal Access Token with `read:packages` scope.

---

## 15. Systemd timers for alerts and backups

After the first successful deploy, install the alert and backup timers:

```bash
sudo cp /opt/jababdihi/staging/repo/infra/systemd/jababdihi-alerts@.* /etc/systemd/system/
sudo cp /opt/jababdihi/staging/repo/infra/systemd/jababdihi-backup@.* /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now jababdihi-alerts@staging.timer
sudo systemctl enable --now jababdihi-backup@staging.timer
sudo systemctl enable --now jababdihi-alerts@prod.timer
sudo systemctl enable --now jababdihi-backup@prod.timer
```

See [operations.md](../infra/operations.md) for details on alerts and backups.
