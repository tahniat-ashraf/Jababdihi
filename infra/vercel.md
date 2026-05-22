# Vercel Frontend Setup

Jababdihi uses Vercel for the Next.js frontend and the VPS-hosted backend for API/data services.

## Project

- Root directory: `frontend`
- Framework preset: Next.js
- Install command: `npm install`
- Build command: `npm run build`
- Output: Vercel default for Next.js App Router

## Environments

Preview deployments:

```text
NEXT_PUBLIC_API_BASE_URL=https://api-staging.example.com
BACKEND_API_BASE_URL=https://api-staging.example.com
```

Production deployments:

```text
NEXT_PUBLIC_API_BASE_URL=https://api.example.com
BACKEND_API_BASE_URL=https://api.example.com
```

## Deployment Policy

- Pull requests create Vercel preview deployments.
- Preview deployments use the staging API.
- Production deploys from `main`.
- Production deployments use the production API.
- Do not expose PostgreSQL, Redis, Ollama, or worker services through Vercel.

## GitHub Integration

Connect the GitHub repository to Vercel and enable:

- Preview deployments for pull requests.
- Production deployment from `main`.
- Required preview checks before merge when branch protection is enabled.
