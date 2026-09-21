package com.hepsiburada.qa.pages.mobile.home;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.hepsiburada.qa.pages.base.MobileBasePage;
import com.hepsiburada.qa.pages.mobile.login.MobileLoginPage;
import com.hepsiburada.qa.pages.mobile.search.MobileSearchResultPage;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

public class MobileHomePage extends MobileBasePage {

    private final By cookieAcceptButton = By.xpath(
        "//button[@id='onetrust-accept-btn-handler'] | " +
        "//button[contains(@class, 'cookie') and (contains(., 'Kabul') or contains(., 'Tümünü Kabul Et') or contains(., 'Tamam'))] | " +
        "//button[contains(text(), 'Kabul Et') or contains(text(), 'Tümünü kabul et')]"
    );

    private final By searchInput = By.cssSelector(
        "input[data-test-id='search-bar-input'], div[class*='searchBox'] input, div[class*='SearchBox'] input, " +
        "input[placeholder*='ürün' i], input[placeholder*='ara' i], input[type='search'], [data-test-id='mobile-search-input']"
    );

    private final By searchTriggerIcon = By.cssSelector(
        "[data-test-id='search-icon'], div[class*='searchIcon'], svg[class*='search'], div[class*='searchBox'], a[href*='arama']"
    );

    private final By accountMenuButton = By.cssSelector(
        "#myAccount, [data-test-id='account'], div[class*='account-user'], a[href*='giris.hepsiburada.com'], [data-test-id='user-menu-btn'], div[class*='UserIcon']"
    );

    private final By loginOption = By.cssSelector(
        "#login, a[href*='giris.hepsiburada.com'], [data-test-id='login-button'], a[href*='login'], button[class*='login']"
    );

    public MobileHomePage(AppiumDriver driver) {
        super(driver);
        waitForHomePageLoad();
    }

    public void waitForHomePageLoad() {
        waitForPageToLoad();
        handleCookies();
    }

    @Step("Handle mobile cookie consent banner")
    public void handleCookies() {
        try {
            if (isDisplayed(cookieAcceptButton, 4)) {
                click(cookieAcceptButton);
                log.info("Mobile cookie consent banner accepted.");
            }
        } catch (Exception ignored) {
        }
    }

    @Step("Search for product on mobile: '{keyword}'")
    public MobileSearchResultPage search(String keyword) {
        handleCookies();
        log.info("Initiating mobile search for keyword: '{}'", keyword);

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                WebElement input;
                try {
                    input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
                } catch (Exception e) {
                    if (isDisplayed(searchTriggerIcon, 2)) {
                        click(searchTriggerIcon);
                    }
                    input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
                }

                try {
                    input.click();
                } catch (Exception e) {
                    jsClick(input);
                }

                WebElement activeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
                activeInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                activeInput.sendKeys(keyword);
                activeInput.sendKeys(Keys.ENTER);

                return new MobileSearchResultPage(driver);
            } catch (org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.ElementNotInteractableException e) {
                log.warn("Mobile search retry {}/{} due to: {}", i + 1, maxRetries, e.getMessage());
                if (i == maxRetries - 1) {
                    throw e;
                }
            }
        }
        return new MobileSearchResultPage(driver);
    }

    public MobileSearchResultPage searchForProduct(String keyword) {
        return search(keyword);
    }

    @Step("Navigate to mobile login page")
    public MobileLoginPage goToLoginPage() {
        handleCookies();
        log.info("Navigating to mobile login page");
        try {
            if (isDisplayed(accountMenuButton, 3)) {
                click(accountMenuButton);
                if (isDisplayed(loginOption, 3)) {
                    click(loginOption);
                }
            } else {
                driver.get("https://giris.hepsiburada.com");
            }
        } catch (Exception e) {
            log.warn("Directing directly to login URL: {}", e.getMessage());
            driver.get("https://giris.hepsiburada.com");
        }
        return new MobileLoginPage(driver);
    }
}
