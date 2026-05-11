import { test, expect } from '@playwright/test';

test('should create a new price alert template', async ({ page }) => {
  await page.goto('http://localhost:5173');

  await page.click('button:has-text("+ Create New Alert")');
  await page.fill('input[placeholder*="Gold Drop"]', 'Testowy Alert');
  await page.fill('textarea', 'To jest treść testowa');
  await page.fill('input[type="email"]', 'test@example.com');

  const metalSelect = page.locator('select').nth(1);
  await metalSelect.selectOption('gold');

  const saveBtn = page.locator('button:has-text("Create Alert")');
  await expect(saveBtn).toBeEnabled({ timeout: 3000 });
  await saveBtn.click();

  await expect(page.locator('.notification.success')).toBeVisible({ timeout: 5000 });
});