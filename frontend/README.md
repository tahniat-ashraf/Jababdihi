# Frontend

Next.js App Router workspace for Jababdihi.

## Stack

- Next.js App Router
- React
- TypeScript
- TailwindCSS
- shadcn/ui-ready component structure
- Vercel preview and production deployments later

## Routes

- `/`: redirects to `/bn`.
- `/bn`: Bangla public feed placeholder.
- `/en`: English public feed placeholder.
- `/admin`: admin workspace placeholder.

## Local Setup

Install dependencies:

```bash
npm install
```

Run the development server:

```bash
npm run dev
```

Run checks:

```bash
npm run lint
npm run typecheck
npm run build
```

## Boundaries

- No API integration has been added yet.
- Do not call AI during public page rendering.
- Do not add popularity-based ranking or detailed click tracking.
- Source links must open directly in a new tab with `noopener noreferrer`.
- Category selection state should remain local frontend state and should not be encoded in the URL.
