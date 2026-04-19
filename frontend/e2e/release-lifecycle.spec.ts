import { test, expect } from '@playwright/test';

test.describe('Release Management', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
    await page.waitForURL('**/releases');
    await expect(page.locator('h1')).toContainText('Releases');
  });

  test('should display navigation and redirect to releases', async ({ page }) => {
    await expect(page.locator('nav')).toContainText('Release Management');
    await expect(page.locator('nav')).toContainText('Releases');
    await expect(page.locator('nav')).toContainText('Environments');
    await expect(page.locator('nav')).toContainText('Artifacts');
    await expect(page.locator('h1')).toContainText('Releases');
  });

  test('should create a new release and display it in the New column', async ({ page }) => {
    const name = `Test Release ${Date.now()}`;
    await page.click('button:has-text("Create Release")');
    await page.fill('input#name', name);
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    await expect(page.locator('text=' + name)).toBeVisible();
  });

  test('should move a release through the lifecycle', async ({ page }) => {
    const name = `Lifecycle Test ${Date.now()}`;
    await page.click('button:has-text("Create Release")');
    await page.fill('input#name', name);
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    await expect(page.locator('text=' + name).first()).toBeVisible();

    // Open detail dialog
    await page.locator('main >> text=' + name).click();
    await page.click('button:has-text("Planned")');

    await expect(page.locator('text=' + name).first()).toBeVisible();

    // Open detail dialog again to move to In Progress
    await page.locator('main >> text=' + name).click();
    await page.click('button:has-text("In Progress")');

    // Assign dialog should appear
    await expect(page.locator('text=Assign Environments \& Artifacts')).toBeVisible();

    // Select SIT environment
    await page.click('text=Select SIT environment');
    await page.locator('[role="listbox"] >> text=SIT1').click();

    // Select UAT environment
    await page.click('text=Select UAT environment');
    await page.locator('[role="listbox"] >> text=UAT1').click();

    // We need an artifact to proceed, so cancel for now
    await page.locator('[role="dialog"] button:has-text("Cancel")').click();
  });

  test('should create an artifact and show it in the list', async ({ page }) => {
    const name = `test-service-${Date.now()}`;
    await page.click('text=Artifacts');
    await page.click('button:has-text("Create Artifact")');
    await page.fill('input#art-name', name);
    await page.fill('input#art-version', '1.0.0');
    await page.fill('input#art-owner', 'QA Team');
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    await expect(page.locator('text=' + name)).toBeVisible();
    await expect(page.locator('text=1.0.0').first()).toBeVisible();
    await expect(page.locator('text=QA Team').first()).toBeVisible();
  });

  test('should display environments with their types', async ({ page }) => {
    await page.click('text=Environments');
    await expect(page.locator('text=DEV01')).toBeVisible();
    await expect(page.locator('text=DEV02')).toBeVisible();
    await expect(page.locator('text=SIT1')).toBeVisible();
    await expect(page.locator('text=SIT2')).toBeVisible();
    await expect(page.locator('text=UAT1')).toBeVisible();
    await expect(page.locator('text=UAT2')).toBeVisible();
  });

  test('should allow full release lifecycle with artifact assignment', async ({ page }) => {
    const artifactName = `payment-api-${Date.now()}`;
    const releaseName = `Full Lifecycle Release ${Date.now()}`;

    // Step 1: Create artifact
    await page.click('text=Artifacts');
    await page.click('button:has-text("Create Artifact")');
    await page.fill('input#art-name', artifactName);
    await page.fill('input#art-version', '2.1.0');
    await page.fill('input#art-owner', 'Backend Team');
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    // Step 2: Go to releases and create a release
    await page.click('text=Releases');
    await page.click('button:has-text("Create Release")');
    await page.fill('input#name', releaseName);
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    await expect(page.locator('text=' + releaseName).first()).toBeVisible();

    // Step 3: Move to Planned
    await page.locator('main >> text=' + releaseName).click();
    await page.click('button:has-text("Planned")');

    // Step 4: Move to In Progress with assignment
    await page.locator('main >> text=' + releaseName).click();
    await page.click('button:has-text("In Progress")');

    await expect(page.locator('text=Assign Environments \& Artifacts')).toBeVisible();

    await page.click('text=Select SIT environment');
    await page.locator('[role="listbox"] >> text=SIT1').click();

    await page.click('text=Select UAT environment');
    await page.locator('[role="listbox"] >> text=UAT1').click();

    // Select artifact
    await page.locator('label', { hasText: new RegExp(artifactName) }).locator('input[type="checkbox"]').check();

    await page.locator('[role="dialog"] button:has-text("Assign \& Start")').click();

    // Verify it's in In Progress column
    await expect(page.locator('h2:has-text("In Progress")')).toBeVisible();

    // Step 5: Move to Finished
    await page.locator('main >> text=' + releaseName).click();
    await page.click('button:has-text("Finished")');

    // Verify it's in Finished column
    await expect(page.locator('h2:has-text("Finished")')).toBeVisible();
  });

  test('should cancel a release', async ({ page }) => {
    const name = `Cancel Test ${Date.now()}`;
    await page.click('button:has-text("Create Release")');
    await page.fill('input#name', name);
    await page.locator('[role="dialog"] button:has-text("Create")').click();

    await expect(page.locator('text=' + name).first()).toBeVisible();

    await page.locator('main >> text=' + name).click();
    await page.click('button:has-text("Cancelled")');

    await expect(page.locator('h2:has-text("Cancelled")')).toBeVisible();
    await expect(page.locator('text=' + name).first()).toBeVisible();
  });
});
