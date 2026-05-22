/**
 * Playwright configuration for deployed staging smoke tests.
 *
 * Runs against the live Vercel preview / staging frontend and the staging VPS backend.
 *
 * Required environment variables:
 *   STAGING_FRONTEND_URL  – Vercel preview or staging alias URL, e.g.
 *                           https://jababdihi-<hash>-ta-workspace.vercel.app
 *   STAGING_API_BASE_URL  – Direct staging backend URL, e.g.
 *                           http://187.77.87.17:8081
 *
 * Run locally:
 *   STAGING_FRONTEND_URL=https://... STAGING_API_BASE_URL=http://... \
 *     npm run test:e2e:staging --prefix frontend
 */
import { defineConfig, devices } from "@playwright/test";

const stagingFrontendUrl = process.env.STAGING_FRONTEND_URL;
if (!stagingFrontendUrl) {
  throw new Error(
    "STAGING_FRONTEND_URL is required for staging E2E tests.\n" +
      "Set it to the Vercel preview URL or your staging domain."
  );
}

export default defineConfig({
  testDir: "./e2e",
  testMatch: "**/smoke.staging.spec.ts",
  // Deployed site is slower than localhost — give it more time
  timeout: 60_000,
  expect: { timeout: 30_000 },
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 1 : 0,
  workers: 1,
  reporter: process.env.CI ? [["github"], ["list"]] : "list",
  use: {
    baseURL: stagingFrontendUrl,
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    // Accept self-signed certs on staging if needed
    ignoreHTTPSErrors: true,
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
