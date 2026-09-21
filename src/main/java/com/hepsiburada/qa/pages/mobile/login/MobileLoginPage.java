package com.hepsiburada.qa.pages.mobile.login;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.MobileBasePage;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

public class MobileLoginPage extends MobileBasePage {

    private final By emailInput = By.cssSelector(
        "input#txtUserName, input[name='username'], input[type='email'], input[data-test-id='email-input'], input[placeholder*='e-posta' i]"
    );

    private final By loginButton = By.cssSelector(
        "button#btnLogin, button[type='submit'], button[data-test-id='login-button'], button[class*='login'], button[class*='btn']"
    );

    private final By errorMessage = By.xpath(
        "//*[contains(., 'Geçerli bir e-posta') or contains(., 'girmelisiniz') or contains(., 'giriniz') or contains(., 'zorunlu')] | " +
        "//div[contains(@class, 'error') or contains(@class, 'Error') or contains(@class, 'helperText') or contains(@class, 'HelperText')] | " +
        "//p[contains(@class, 'error') or contains(@class, 'helper')] | " +
        "//span[contains(@class, 'error') or contains(@class, 'helper')]"
    );

    public MobileLoginPage(AppiumDriver driver) {
        super(driver);
        waitForLoginPageLoad();
    }

    public void waitForLoginPageLoad() {
        waitForPageToLoad();
        try {
            wait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emailInput),
                ExpectedConditions.presenceOfElementLocated(loginButton)
            ));
        } catch (Exception ignored) {
        }
    }

    @Step("Enter email on mobile login: {email}")
    public MobileLoginPage enterEmail(String email) {
        log.info("Entering email on mobile login: {}", email);
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.sendKeys(email);
        return this;
    }

    @Step("Clear email input on mobile login")
    public MobileLoginPage clearEmailInput() {
        log.info("Clearing email input on mobile login");
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("Click login button on mobile")
    public MobileLoginPage clickLoginButton() {
        log.info("Clicking login button on mobile");
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
            jsClick(btn);
        } catch (Exception e) {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
            jsClick(btn);
        }

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            shortWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(errorMessage),
                ExpectedConditions.urlContains("oauth"),
                ExpectedConditions.urlContains("password")
            ));
        } catch (Exception ignored) {
        }

        return this;
    }

    @Step("Get error message on mobile login")
    public String getErrorMessage() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement errorEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            String msg = errorEl.getText().trim();
            log.info("Captured mobile error message: {}", msg);
            return msg;
        } catch (Exception e) {
            try {
                return driver.findElement(By.xpath("//*[contains(text(), 'Geçerli bir e-posta')]")).getText().trim();
            } catch (Exception ignored) {
            }
        }
        return "";
    }

    @Step("Check if error message is displayed on mobile login")
    public boolean isErrorMessageDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement errorEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return errorEl != null && errorEl.isDisplayed();
        } catch (Exception e) {
            try {
                WebElement fallback = driver.findElement(By.xpath("//*[contains(text(), 'Geçerli bir e-posta')]"));
                return fallback.isDisplayed();
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
