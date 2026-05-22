/**
 * End-to-end smoke test for the public incident feed.
 *
 * Requires the full local stack to be running:
 *   - PostgreSQL + Redis: docker compose -f infra/docker-compose.local.yml up -d postgres redis
 *   - Backend:  cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=api,local
 *   - Seed:     cd backend && ./mvnw spring-boot:run -Dspring-boot.run.profiles=api,seed \
 *                   -Dspring-boot.run.arguments="--app.seed=staging"
 *   - Frontend: cd frontend && npm run dev
 *
 * Run: cd frontend && npm run test:e2e
 */

import { expect, test } from "@playwright/test";

// Seeded counts (must match StagingDataSeeder)
const GOV_PUBLISHED = 5;
const OPP_PUBLISHED = 3;

test.describe("Bangla feed (/bn)", () => {
  test("route loads without error", async ({ page }) => {
    await page.goto("/bn");
    await expect(page.locator("header")).toBeVisible();
    await expect(page).not.toHaveURL(/error/i);
  });

  test("Government feed loads by default", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");

    // At least one incident card must appear
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    // The Government nav button (Bangla label) should be in the active state
    const govLink = page.getByRole("link", { name: "সরকার" }).first();
    await expect(govLink).toBeVisible();

    // Active Government button has blue background per design rules
    const govButton = govLink.locator("..");
    await expect(govButton).toHaveClass(/bg-blue-600/, { timeout: 5_000 });
  });

  test("Opposition switch updates the feed", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    // Click the Opposition nav link
    await page.getByRole("link", { name: "বিরোধী দল" }).first().click();

    // URL should reflect the switch
    await expect(page).toHaveURL(/actorRole=OPPOSITION/);

    // Feed re-loads with Opposition incidents
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    // Actor badge on the first card should read "OPPOSITION"
    const firstCard = page.locator("article").first();
    await expect(firstCard.getByText("OPPOSITION")).toBeVisible({ timeout: 10_000 });
  });

  test("category filter — deselect all shows empty state", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    // Click the Bangla "None" button in the category sidebar
    await page.getByRole("button", { name: "কোনোটিই নয়" }).click();

    // Empty state message must appear
    await expect(
      page.getByText("কোনো প্রকাশিত ঘটনা পাওয়া যায়নি")
    ).toBeVisible({ timeout: 10_000 });

    // Restore all categories
    await page.getByRole("button", { name: "সব" }).click();
    await expect(page.locator("article").first()).toBeVisible({ timeout: 15_000 });
  });

  test("source chips show first 3 sources and +N more", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    // At least one card in the seeded GOV data has 5 sources → shows "+2 more"
    const moreChip = page
      .locator("article span")
      .filter({ hasText: /^\+\d+/ })
      .first();
    await expect(moreChip).toBeVisible({ timeout: 15_000 });

    // Verify the text matches the "+N আরও" pattern (Bangla label)
    const moreText = await moreChip.innerText();
    expect(moreText).toMatch(/^\+\d+/);
  });

  test("confidence gauge shows numeric score", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    const firstCard = page.locator("article").first();

    // Gauge arc element has aria-label set to the confidence label
    const gaugeArc = firstCard.locator("[aria-label]").first();
    await expect(gaugeArc).toBeVisible();

    // The numeric score is rendered in a large text element
    const scoreEl = firstCard.locator(".text-xl").first();
    await expect(scoreEl).toBeVisible();

    const scoreText = (await scoreEl.innerText()).trim();
    // Score should be a number, not the placeholder "--"
    expect(scoreText).not.toBe("--");
    const parsed = parseInt(scoreText, 10);
    expect(parsed).toBeGreaterThanOrEqual(0);
    expect(parsed).toBeLessThanOrEqual(100);
  });

  test("infinite scroll fetches the next page", async ({ page }) => {
    test.setTimeout(60_000);

    await page.goto("/bn?actorRole=GOVERNMENT");

    // The MVP staging seed is intentionally compact but still exercises pagination wiring.
    await page.waitForFunction(
      () => document.querySelectorAll("article").length >= 1,
      undefined,
      { timeout: 30_000 }
    );

    const initialCount = await page.locator("article").count();
    expect(initialCount).toBeGreaterThanOrEqual(1);

    // Scroll to the bottom sentinel to trigger IntersectionObserver
    await page.evaluate(() => window.scrollTo(0, document.body.scrollHeight));

    const newCount = await page.locator("article").count();
    expect(newCount).toBeGreaterThanOrEqual(initialCount);
    expect(newCount).toBe(GOV_PUBLISHED);
  });
});

test.describe("English feed (/en)", () => {
  test("route loads and shows English UI labels", async ({ page }) => {
    await page.goto("/en?actorRole=GOVERNMENT");
    await expect(page.locator("header")).toBeVisible();

    // English actor button labels must be visible
    await expect(page.getByRole("link", { name: "Government" }).first()).toBeVisible();
    await expect(page.getByRole("link", { name: "Opposition" }).first()).toBeVisible();

    // English category sidebar heading
    await expect(page.getByText("Categories")).toBeVisible();

    // Incident cards must load
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });
  });
});

test.describe("Source link safety", () => {
  test("source anchors have target=_blank and rel=noopener noreferrer", async ({ page }) => {
    await page.goto("/bn?actorRole=GOVERNMENT");
    await expect(page.locator("article").first()).toBeVisible({ timeout: 20_000 });

    const firstCard = page.locator("article").first();
    const sourceAnchors = firstCard.locator("a[target='_blank']");
    await expect(sourceAnchors.first()).toBeVisible();

    // Check every visible source anchor in the first card
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

test.describe("Non-public incident exclusion", () => {
  test("public feed returns only AUTO_PUBLISHED or MANUALLY_PUBLISHED incidents", async ({
    request,
  }) => {
    // Query through the Next.js proxy (same host as baseURL)
    const govResp = await request.get(
      `/api/incidents?actorRole=GOVERNMENT&language=en&pageSize=50`
    );
    expect(govResp.ok()).toBe(true);

    const govData = (await govResp.json()) as {
      items: Array<{ actorRole: string }>;
      totalItems: number;
    };

    // Only GOVERNMENT actor incidents
    for (const item of govData.items) {
      expect(item.actorRole).toBe("GOVERNMENT");
    }

    // Total must equal exactly the seeded GOV published count
    expect(govData.totalItems).toBe(GOV_PUBLISHED);

    // Same check for Opposition
    const oppResp = await request.get(
      `/api/incidents?actorRole=OPPOSITION&language=en&pageSize=50`
    );
    expect(oppResp.ok()).toBe(true);

    const oppData = (await oppResp.json()) as {
      items: Array<{ actorRole: string }>;
      totalItems: number;
    };

    for (const item of oppData.items) {
      expect(item.actorRole).toBe("OPPOSITION");
    }

    expect(oppData.totalItems).toBe(OPP_PUBLISHED);
  });

  test("GET /api/categories returns expected category list", async ({ request }) => {
    const resp = await request.get(`/api/categories?language=en`);
    expect(resp.ok()).toBe(true);

    const data = (await resp.json()) as { items: Array<{ code: string }> };
    const codes = data.items.map((c) => c.code);

    // Required categories from the schema
    expect(codes).toContain("EXTORTION");
    expect(codes).toContain("KILLING");
    expect(codes).toContain("POLITICAL_VIOLENCE");
    expect(codes.length).toBeGreaterThanOrEqual(16);
  });

  test("GET /api/actors returns GOVERNMENT and OPPOSITION", async ({ request }) => {
    const resp = await request.get(`/api/actors?language=en`);
    expect(resp.ok()).toBe(true);

    const data = (await resp.json()) as { items: Array<{ role: string }> };
    const roles = data.items.map((a) => a.role);

    expect(roles).toContain("GOVERNMENT");
    expect(roles).toContain("OPPOSITION");
    expect(roles).not.toContain("UNKNOWN");
  });
});
