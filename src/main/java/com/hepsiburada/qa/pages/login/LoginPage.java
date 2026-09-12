package com.hepsiburada.qa.pages.login;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

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

    public LoginPage(WebDriver driver) {
        super(driver);
        waitForLoginPageLoad();
    }

    public void waitForLoginPageLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emailInput),
                ExpectedConditions.presenceOfElementLocated(loginButton)
            ));
        } catch (Exception ignored) {}
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.sendKeys(email);
        return this;
    }

    @Step("Clear email input field")
    public LoginPage clearEmailInput() {
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        } catch (Exception ignored) {}
        return this;
    }

    @Step("Click login submit button")
    public LoginPage clickLoginButton() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
            jsClick(btn);
        } catch (Exception e) {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
            jsClick(btn);
        }

        try {
            Thread.sleep(1500); 
        } catch (InterruptedException ignored) {}

        return this;
    }

    @Step("Get validation error message text")
    public String getErrorMessage() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement errorEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return errorEl.getText().trim();
        } catch (Exception e) {
            try {
                return driver.findElement(By.xpath("//*[contains(text(), 'Geçerli bir e-posta')]")).getText().trim();
            } catch (Exception ignored) {}
        }
        return "";
    }

    @Step("Verify validation error message is displayed")
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