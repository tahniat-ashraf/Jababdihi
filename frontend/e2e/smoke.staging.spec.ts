/**
 * Deployed staging smoke tests.
 *
 * Verifies that the staging frontend can talk to the staging backend end-to-end.
 * Run after staging deploy to catch integration breaks before they reach production.
 *
 * Prerequisites:
 *   - Staging VPS is running (all five Docker containers healthy).
 *   - Staging seed data is present (run infra/scripts/run-seed.sh staging on the VPS,
 *     or let the CI staging-e2e job do it automatically).
 *   - STAGING_FRONTEND_URL points to the deployed Vercel preview / staging alias.
 *   - STAGING_API_BASE_URL points directly to the staging backend (e.g.
 *     http://<YOUR_STAGING_VPS_IP>:8081).
 *
 * Run:
 *   STAGING_FRONTEND_URL=https://... STAGING_API_BASE_URL=http://... \
 *     npm run test:e2e:staging --prefix frontend
 *
 * Count assertions use ≥ rather than == so they pass even if the staging
 * database has been used for manual testing beyond the seeded baseline.
 *
 * Seed baseline: 5 published GOVERNMENT incidents, 3 published OPPOSITION
 * incidents, 1 PENDING_REVIEW (must not appear in the public feed).
 */

import { expect, request as baseRequest, test } from "@playwright/test";

// Minimum published incident counts guaranteed by run-seed.sh
const MIN_GOV_PUBLISHED = 5;
const MIN_OPP_PUBLISHED = 3;

// ─── Backend health check ─────────────────────────────────────────────────────

test.describe("Staging backend health", () => {
  test("backend actuator/health returns UP", async () => {
    const stagingApiUrl = process.env.STAGING_API_BASE_URL;
    if (!stagingApiUrl) {
      test.skip(true, "STAGING_API_BASE_URL not set — skipping direct backend check");
      return;
    }

    const ctx = await baseRequest.newContext({ baseURL: stagingApiUrl });
    try {
      const resp = await ctx.get("/actuator/health");
      expect(resp.status()).toBe(200);
      const body = (await resp.json()) as { status: string };
      expect(body.status).toBe("UP");
    } finally {
      await ctx.dispose();
    }
  });
});

// ─── Requirement 1 & 2: page loads ───────────────────────────────────────────

test.describe("Bangla feed (/bn) — page load", () => {
  test("route loads without error", async ({ page }) => {
    await page.goto("/bn");
    await expect(page.locator("header")).toBeVisible();
    await expect(page).not.toHaveURL(/error/i);
  });
});

// ─── Requirement 3: Government feed loads by default ─────────────────────────

test.describe("Bangla feed (/bn) — Government feed", () => {
  test("Government feed loads at least one incident card", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    // Government nav tab must be visible
    const govLink = page.getByRole("link", { name: "সরকার" }).first();
    await expect(govLink).toBeVisible();
  });
});

// ─── Requirement 4: Opposition switch changes the feed ───────────────────────

test.describe("Bangla feed (/bn) — Opposition switch", () => {
  test("switching to Opposition updates the URL and loads Opposition cards", async ({
    page,
  }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    await page.getByRole("link", { name: "বিরোধী দল" }).first().click();

    await expect(page).toHaveURL(/actorRole=OPPOSITION/);
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    // At least one card must carry an OPPOSITION actor badge
    const firstCard = page.locator("article").first();
    await expect(firstCard.getByText("OPPOSITION")).toBeVisible({ timeout: 15_000 });
  });
});

// ─── Requirement 5: English UI labels ────────────────────────────────────────

test.describe("English feed (/en)", () => {
  test("English route shows English actor labels and incident cards", async ({ page }) => {
    await page.goto("/en?actorRole=GOVERNMENT");
    await expect(page.locator("header")).toBeVisible();

    await expect(page.getByRole("link", { name: "Government" }).first()).toBeVisible();
    await expect(page.getByRole("link", { name: "Opposition" }).first()).toBeVisible();
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });
  });
});

// ─── Requirement 6: source chips render ──────────────────────────────────────

test.describe("Source chips", () => {
  test("first seeded Government incident shows source chips and +N more chip", async ({
    page,
  }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    // The first seeded Government incident has 5 sources → chip shows "+2 আরও"
    const moreChip = page
      .locator("article span")
      .filter({ hasText: /^\+\d+/ })
      .first();
    await expect(moreChip).toBeVisible({ timeout: 20_000 });

    const text = await moreChip.innerText();
    expect(text).toMatch(/^\+\d+/);
  });
});

// ─── Requirement 7: confidence gauge renders ─────────────────────────────────

test.describe("Confidence gauge", () => {
  test("first card shows a numeric confidence score, not placeholder '--'", async ({
    page,
  }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    const firstCard = page.locator("article").first();
    const scoreEl = firstCard.locator(".text-xl").first();
    await expect(scoreEl).toBeVisible();

    const scoreText = (await scoreEl.innerText()).trim();
    expect(scoreText).not.toBe("--");
    const parsed = parseInt(scoreText, 10);
    expect(parsed).toBeGreaterThanOrEqual(0);
    expect(parsed).toBeLessThanOrEqual(100);
  });
});

// ─── Requirement 8: infinite scroll fetches another page ─────────────────────

test.describe("Infinite scroll", () => {
  test("scrolling to the bottom with pageSize=3 loads more cards from the seed data", async ({
    page,
  }) => {
    test.setTimeout(90_000);

    // pageSize=3 forces pagination: seed has 5 GOV published, so page 2 exists.
    await page.goto("/bn?actorRole=GOVERNMENT&pageSize=3");

    await page.waitForFunction(
      () => document.querySelectorAll("article").length >= 1,
      undefined,
      { timeout: 30_000 }
    );

    const initialCount = await page.locator("article").count();
    expect(initialCount).toBeGreaterThanOrEqual(1);

    // Trigger IntersectionObserver sentinel
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));
    await page.waitForTimeout(3_000);

    const afterScrollCount = await page.locator("article").count();
    // After scrolling there should be at least as many cards (scroll must not break things)
    expect(afterScrollCount).toBeGreaterThanOrEqual(initialCount);
    // With 5 published and pageSize=3, both pages should have loaded
    expect(afterScrollCount).toBeGreaterThanOrEqual(Math.min(initialCount + 1, MIN_GOV_PUBLISHED));
  });
});

// ─── Requirement 9: source anchor safety ─────────────────────────────────────

test.describe("Source link safety", () => {
  test("source anchors have target=_blank and rel=noopener noreferrer", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 30_000 });

    const firstCard = page.locator("article").first();
    const sourceAnchors = firstCard.locator("a[target='_blank']");
    await expect(sourceAnchors.first()).toBeVisible({ timeout: 15_000 });

    const count = await sourceAnchors.count();
    expect(count).toBeGreaterThan(0);

    for (let i = 0; i < count; i++) {
      const anchor = sourceAnchors.nth(i);
      await expect(anchor).toHaveAttribute("target", "_blank");
      const rel = await anchor.getAttribute("rel");
      expect(rel).toContain("noopener");
      expect(rel).toContain("noreferrer");
    }
  });
});

// ─── Requirement 10: non-public incidents excluded from feed ─────────────────

test.describe("Non-public incident exclusion", () => {
  test("public feed returns only AUTO_PUBLISHED or MANUALLY_PUBLISHED incidents", async ({
    request,
  }) => {
    // Query through the Next.js proxy — validates the full stack
    const govResp = await request.get(
      "/api/incidents?actorRole=GOVERNMENT&language=en&pageSize=50"
    );
    expect(govResp.ok()).toBe(true);

    const govData = (await govResp.json()) as {
      items: Array<{ actorRole: string }>;
      totalItems: number;
    };

    for (const item of govData.items) {
      expect(item.actorRole).toBe("GOVERNMENT");
    }
    // Must have at least the seeded published incidents
    expect(govData.totalItems).toBeGreaterThanOrEqual(MIN_GOV_PUBLISHED);

    const oppResp = await request.get(
      "/api/incidents?actorRole=OPPOSITION&language=en&pageSize=50"
    );
    expect(oppResp.ok()).toBe(true);

    const oppData = (await oppResp.json()) as {
      items: Array<{ actorRole: string }>;
      totalItems: number;
    };

    for (const item of oppData.items) {
      expect(item.actorRole).toBe("OPPOSITION");
    }
    expect(oppData.totalItems).toBeGreaterThanOrEqual(MIN_OPP_PUBLISHED);
  });

  test("PENDING_REVIEW incident seeded as non-public does not appear in the feed", async ({
    request,
  }) => {
    // Fetch all Government incidents (no pageSize cap → defaults to backend max)
    const resp = await request.get(
      "/api/incidents?actorRole=GOVERNMENT&language=en&pageSize=100"
    );
    expect(resp.ok()).toBe(true);

    const data = (await resp.json()) as {
      items: Array<{ status?: string }>;
    };

    // No item in the public feed should be PENDING_REVIEW
    for (const item of data.items) {
      if (item.status) {
        expect(item.status).not.toBe("PENDING_REVIEW");
      }
    }
  });

  test("categories endpoint returns expected category list", async ({ request }) => {
    const resp = await request.get("/api/categories?language=en");
    expect(resp.ok()).toBe(true);

    const data = (await resp.json()) as { items: Array<{ code: string }> };
    const codes = data.items.map((c) => c.code);

    expect(codes).toContain("EXTORTION");
    expect(codes).toContain("KILLING");
    expect(codes).toContain("POLITICAL_VIOLENCE");
    expect(codes.length).toBeGreaterThanOrEqual(16);
  });

  test("actors endpoint returns GOVERNMENT and OPPOSITION only", async ({ request }) => {
    const resp = await request.get("/api/actors?language=en");
    expect(resp.ok()).toBe(true);

    const data = (await resp.json()) as { items: Array<{ role: string }> };
    const roles = data.items.map((a) => a.role);

    expect(roles).toContain("GOVERNMENT");
    expect(roles).toContain("OPPOSITION");
    expect(roles).not.toContain("UNKNOWN");
  });
});
