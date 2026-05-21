# Frontend

Frontend workspace for Jababdihi.

## Planned Stack

- Next.js App Router
- React
- TypeScript
- TailwindCSS
- shadcn/ui
- TanStack Query
- Vercel preview and production deployments

## MVP Responsibilities

- Public Government/Opposition feed.
- Bangla and English routing.
- Actor selector and language toggle in the header.
- Category filters with all default-visible categories selected initially.
- Incident cards with source chips and confidence gauge.
- Infinite scroll backed by page-number API calls.
- Public incident detail pages.
- Methodology/legal pages in Bangla and English.
- Admin UI under `/admin` later in the build order.

## Local Setup

No frontend application has been generated yet.

When the Next.js app is added, document the exact install and run commands here. Expected commands will likely include:

```bash
npm install
npm run dev
npm run lint
npm run typecheck
npm run build
```

## Boundaries

- Do not call AI during public page rendering.
- Do not add popularity-based ranking or detailed click tracking.
- Source links must open directly in a new tab with `noopener noreferrer`.
- Category selection state should remain local frontend state and should not be encoded in the URL.
