package com.hepsiburada.qa.pages.home.components;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;

public class CookieBannerComponent extends BasePage {

    private final By acceptCookiesButton = By.cssSelector(
        "#hb-accept-all, #onetrust-accept-btn-handler, [data-name='Banner'] #hb-accept-all, div[id='hb-accept-all']"
    );
    private final By bannerContainer = By.cssSelector(
        "div[data-name='Banner'], div[efl-consent-not-saved], #onetrust-banner-sdk, .onetrust-pc-dark-filter, #onetrust-consent-sdk"
    );

    public CookieBannerComponent(WebDriver driver) {
        super(driver);
    }

    public void acceptCookies() {
        try {
            dismissBanner();
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 3000) {
                try {
                    WebElement button = driver.findElement(acceptCookiesButton);
                    if (button != null && button.isDisplayed()) {
                        try {
                            button.click();
                        } catch (Exception e) {
                            jsClick(button);
                        }
                        dismissBanner();
                        break;
                    }
                } catch (Exception ignored) {
                }
                dismissBanner();
                try {
                    Thread.sleep(250);
                } catch (InterruptedException ignored) {
                }
            }
            dismissBanner();
        } catch (Exception ignored) {
        }
    }

    public boolean isCookieBannerDisplayed() {
        return isDisplayed(acceptCookiesButton);
    }

    private void dismissBanner() {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "(function() {" +
                "  function queryShadow(selector, root) {" +
                "    if (!root) root = document;" +
                "    var el = root.querySelector(selector);" +
                "    if (el) return el;" +
                "    var all = root.querySelectorAll('*');" +
                "    for (var i = 0; i < all.length; i++) {" +
                "      if (all[i].shadowRoot) {" +
                "        var found = queryShadow(selector, all[i].shadowRoot);" +
                "        if (found) return found;" +
                "      }" +
                "    }" +
                "    return null;" +
                "  }" +
                "  function removeAllMatching(selector, root) {" +
                "    if (!root) root = document;" +
                "    var list = root.querySelectorAll(selector);" +
                "    for (var i = 0; i < list.length; i++) {" +
                "      list[i].style.setProperty('display', 'none', 'important');" +
                "      list[i].style.setProperty('visibility', 'hidden', 'important');" +
                "      try { list[i].remove(); } catch(e) {}" +
                "    }" +
                "    var all = root.querySelectorAll('*');" +
                "    for (var j = 0; j < all.length; j++) {" +
                "      if (all[j].shadowRoot) {" +
                "        removeAllMatching(selector, all[j].shadowRoot);" +
                "      }" +
                "    }" +
                "  }" +
                "  try {" +
                "    var btn = queryShadow('#hb-accept-all') ||" +
                "              queryShadow('#onetrust-accept-btn-handler') ||" +
                "              queryShadow('[id*=\"hb-accept\"]') ||" +
                "              queryShadow('[data-name=\"Buttons\"] div:last-child');" +
                "    if (btn) {" +
                "      try { btn.click(); } catch(e) {}" +
                "      try { btn.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, composed: true, view: window })); } catch(e) {}" +
                "    }" +
                "    removeAllMatching('div[data-name=\"Banner\"], div[efl-consent-not-saved], #onetrust-consent-sdk, #onetrust-banner-sdk, .onetrust-pc-dark-filter, [class*=\"animate-slide-in-up\"][class*=\"z-2147483647\"]');" +
                "  } catch(e) {}" +
                "})();"
            );
        } catch (Exception ignored) {
        }
    }
}
