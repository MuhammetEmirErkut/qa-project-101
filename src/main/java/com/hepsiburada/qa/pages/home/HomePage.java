package com.hepsiburada.qa.pages.home;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;
import com.hepsiburada.qa.pages.home.components.CookieBannerComponent;
import com.hepsiburada.qa.pages.login.LoginPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;

import io.qameta.allure.Step;

public class HomePage extends BasePage {

    private final By searchInput = By.cssSelector("div[class*='searchBox'] input, div[class*='SearchBox'] input, [data-test-id='search-bar-input'], input[placeholder*='ürün' i], input[placeholder*='ara' i], input[type='text']");
    private final By searchButton = By.cssSelector("div[class*='searchBox'] button, div[class*='SearchBox'] button, [data-test-id='search-button'], div[class*='searchIcon'], svg[class*='search'], div[class*='SearchBoxOld'] button");
    private final By searchButtonXpath = By.xpath("//div[contains(@class, 'searchBox') or contains(@class, 'SearchBox')]//button | //div[contains(@class, 'searchIcon')] | //button[contains(., 'ARA') or contains(., 'Ara')]");
    private final By myAccountButton = By.cssSelector("#myAccount, [data-test-id='account'], div[class*='account-user']");
    private final By loginDropdownOption = By.cssSelector("#login, a[href*='giris.hepsiburada.com'], [data-test-id='login-button'], a[href*='login']");
    private final CookieBannerComponent cookieBanner;

    public HomePage(WebDriver driver) {
        super(driver);
        this.cookieBanner = new CookieBannerComponent(driver);
        waitForHomePageLoad();
    }

    public void waitForHomePageLoad() {
        waitForPageToLoad();
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(searchInput));
        } catch (Exception e) {
            log.warn("Search input not immediately present on home page: {}", e.getMessage());
        }
        handleCookies();
    }

    public void handleCookies() {
        cookieBanner.acceptCookies();
    }

    @Step("Search for product: '{keyword}'")
    public SearchResultPage search(String keyword) {
        handleCookies();
        log.info("Initiating search for keyword: '{}'", keyword);

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
                try {
                    input.click();
                } catch (Exception e) {
                    jsClick(input);
                }

                WebElement activeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
                activeInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                try {
                    activeInput.clear();
                } catch (Exception ignored) {
                }
                activeInput.sendKeys(keyword);
                activeInput.sendKeys(Keys.ENTER);

                try {
                    WebDriverWait searchNavWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                    searchNavWait.until(ExpectedConditions.urlContains("ara?q="));
                } catch (Exception e) {
                    try {
                        WebElement sBtn = driver.findElement(searchButton);
                        jsClick(sBtn);
                    } catch (Exception ex) {
                        try {
                            WebElement sBtnXpath = driver.findElement(searchButtonXpath);
                            jsClick(sBtnXpath);
                        } catch (Exception ignored) {
                        }
                    }
                }

                return new SearchResultPage(driver);
            } catch (org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.ElementNotInteractableException e) {
                log.warn("Retry {}/{} for search due to: {}", i + 1, maxRetries, e.getMessage());
                if (i == maxRetries - 1) {
                    throw e;
                }
            }
        }
        return new SearchResultPage(driver);
    }

    public SearchResultPage searchForProduct(String keyword) {
        return search(keyword);
    }

    @Step("Navigate to login page")
    public LoginPage goToLoginPage() {
        handleCookies();
        log.info("Navigating to login page via user menu");
        try {
            WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(myAccountButton));
            hover(myAccountButton);
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginDropdownOption));
            jsClick(loginBtn);
        } catch (Exception e) {
            log.warn("Dropdown navigation failed, directing directly to login URL: {}", e.getMessage());
            driver.get("https://giris.hepsiburada.com");
        }
        return new LoginPage(driver);
    }
}
