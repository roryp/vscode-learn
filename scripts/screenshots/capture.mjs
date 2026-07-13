// Automated screenshot capture for the demo recording script.
//
// Captures every BROWSER-based shot so you don't have to snip them by hand:
//   01-web-app.png            the running Todo app with items
//   02-actuator-health.png    the /actuator/health JSON
//   03-mcp-added-todo.png     the app after an "MCP" todo is added
//   07-playwright-run.png     Playwright mid-run driving the UI (input filled, Add hovered)
//   08-issue-assigned.png     (optional) the assigned GitHub issue   -> set GH_ISSUE_URL
//   09-agent-pr.png           (optional) the agent's pull request    -> set GH_PR_URL
//
// It uses your installed Microsoft Edge via Playwright's "msedge" channel —
// no multi-hundred-MB browser download.
//
// Prereqs:
//   1) npm install            (in this folder — pulls playwright-core only)
//   2) Spring Boot app running: from the repo root  .\mvnw.cmd spring-boot:run
//
// Usage (from scripts/screenshots):
//   node capture.mjs                              # app + playwright-run shots
//   $env:GH_ISSUE_URL="https://github.com/roryp/vscode-learn/issues/NN"
//   $env:GH_PR_URL="https://github.com/roryp/vscode-learn/pull/NN"
//   node capture.mjs                              # also captures 08 + 09
//
// Env vars (all optional):
//   BASE_URL      default http://localhost:8080
//   OUT_DIR       default ../../docs/images
//   HEADLESS      set to "1" to hide the browser window (default: headed)
//   GH_ISSUE_URL  capture 08-issue-assigned.png
//   GH_PR_URL     capture 09-agent-pr.png

import { chromium } from 'playwright-core';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import fs from 'node:fs';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const BASE_URL = process.env.BASE_URL ?? 'http://localhost:8080';
const OUT_DIR = process.env.OUT_DIR
  ? path.resolve(process.env.OUT_DIR)
  : path.resolve(__dirname, '..', '..', 'docs', 'images');
const HEADLESS = process.env.HEADLESS === '1';
const PROFILE_DIR = path.resolve(__dirname, '.edge-profile');

fs.mkdirSync(OUT_DIR, { recursive: true });

const shot = async (page, name) => {
  await page.screenshot({ path: path.join(OUT_DIR, name) });
  console.log('  saved docs/images/' + name);
};

async function ensureTodos(page, titles) {
  for (const title of titles) {
    const exists = await page
      .locator('[data-testid="todo-item"]', { hasText: title })
      .count();
    if (exists) continue;
    await page.fill('[data-testid="new-todo-input"]', title);
    await page.click('[data-testid="add-todo"]');
    await page.waitForLoadState('networkidle');
  }
}

function needsGitHubLogin(page) {
  return /\/(login|session)/.test(new URL(page.url()).pathname);
}

async function main() {
  const context = await chromium.launchPersistentContext(PROFILE_DIR, {
    channel: 'msedge',
    headless: HEADLESS,
    viewport: { width: 1200, height: 800 },
  });
  const page = context.pages()[0] ?? (await context.newPage());

  // ---- App shots -----------------------------------------------------------
  let appUp = true;
  const skipApp = process.env.SKIP_APP === '1';
  try {
    await page.goto(BASE_URL, { waitUntil: 'domcontentloaded', timeout: 5000 });
  } catch {
    appUp = false;
    console.warn(
      '! App not reachable at ' + BASE_URL +
      ' - start it (.\\mvnw.cmd spring-boot:run) then rerun. Skipping app shots.'
    );
  }

  if (appUp && !skipApp) {
    console.log('Capturing app shots...');
    await ensureTodos(page, ['Buy milk', 'Ship the release', 'Water the plants']);
    await shot(page, '01-web-app.png');

    await page.goto(BASE_URL + '/actuator/health', { waitUntil: 'domcontentloaded' });
    await shot(page, '02-actuator-health.png');

    await page.goto(BASE_URL, { waitUntil: 'domcontentloaded' });
    await ensureTodos(page, ['Email the stakeholders (added via MCP)']);
    await shot(page, '03-mcp-added-todo.png');

    // 07 - "Playwright mid-run": input filled + Add hovered, captured pre-submit.
    await page.fill('[data-testid="new-todo-input"]', 'Prep the demo recording');
    await page.hover('[data-testid="add-todo"]');
    await shot(page, '07-playwright-run.png');
  }

  // ---- GitHub shots (optional) --------------------------------------------
  const ghTargets = [
    ['GH_ISSUE_URL', '08-issue-assigned.png', 'issue'],
    ['GH_PR_URL', '09-agent-pr.png', 'pull request'],
  ];
  for (const [envVar, name, label] of ghTargets) {
    const url = process.env[envVar];
    if (!url) continue;
    console.log('Capturing GitHub ' + label + '...');
    await page.goto(url, { waitUntil: 'domcontentloaded' });
    if (needsGitHubLogin(page)) {
      console.warn('  Not signed in - a window is open. Sign in to GitHub (up to 2 min)...');
      await page
        .waitForURL((u) => !/\/(login|session)/.test(new URL(u).pathname), { timeout: 120000 })
        .catch(() => {});
      await page.goto(url, { waitUntil: 'domcontentloaded' });
    }
    await page.waitForTimeout(1500);
    await shot(page, name);
  }

  await context.close();
  console.log('Done. Output -> ' + OUT_DIR);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
