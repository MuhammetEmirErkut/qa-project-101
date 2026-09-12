package com.hepsiburada.qa.pages.home;

import java.time.Duration;

import io.qameta.allure.Step;
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

public class HomePage extends BasePage {

    private final By searchInput = By.cssSelector("div[class*='searchBox'] input, div[class*='SearchBox'] input, [data-test-id='search-bar-input'], input[placeholder*='ürün' i], input[placeholder*='ara' i]");
    private final By myAccountButton = By.cssSelector("#myAccount, [data-test-id='account'], div[class*='account-user']");
    private final By loginDropdownOption = By.cssSelector("#login, a[href*='giris.hepsiburada.com'], [data-test-id='login-button'], a[href*='login']");
    private final CookieBannerComponent cookieBanner;

    public HomePage(WebDriver driver) {
        super(driver);
        this.cookieBanner = new CookieBannerComponent(driver);
        waitForHomePageLoad();
    }

    public void waitForHomePageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.presenceOfElementLocated(searchInput));
        handleCookies();
    }

    public void handleCookies() {
        cookieBanner.acceptCookies();
    }

    @Step("Search for a product")
    public SearchResultPage search(String keyword) {
        handleCookies();

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                WebElement input = wait.until(ExpectedConditions.elementToBeClickable(searchInput));
                try {
                    input.click();
                } catch (Exception e) {
                    jsClick(input);
                }

                try {
                    Thread.sleep(400);
                } catch (InterruptedException ignored) {
                }

                WebElement activeInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInput));
                activeInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                activeInput.sendKeys(keyword);
                activeInput.sendKeys(Keys.ENTER);
                return new SearchResultPage(driver);
            } catch (org.openqa.selenium.StaleElementReferenceException | org.openqa.selenium.ElementNotInteractableException e) {
                if (i == maxRetries - 1) {
                    throw e;
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ignored) {
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
        try {
            WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(myAccountButton));
            new org.openqa.selenium.interactions.Actions(driver).moveToElement(accountBtn).perform();
            Thread.sleep(500);
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginDropdownOption));
            jsClick(loginBtn);
        } catch (Exception e) {
            driver.get("https://giris.hepsiburada.com");
        }
        return new LoginPage(driver);
    }
}
