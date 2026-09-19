package com.hepsiburada.qa.pages.base;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public abstract class BasePage {
    protected static final Logger log = LogManager.getLogger(BasePage.class);

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected int defaultTimeoutSeconds;

    public BasePage(WebDriver driver) {
        this(driver, 15);
    }

    public BasePage(WebDriver driver, int timeoutInSeconds) {
        this.driver = driver;
        this.defaultTimeoutSeconds = timeoutInSeconds;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    public void switchToNewTab() {
        String currentWindow = driver.getWindowHandle();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(d -> d.getWindowHandles().size() > 1);
        } catch (Exception e) {
            log.debug("No additional window handle detected within timeout.");
        }

        for (String handle : driver.getWindowHandles()) {
            if (!handle.equals(currentWindow)) {
                driver.switchTo().window(handle);
                log.info("Switched to new browser tab: {}", handle);
                break;
            }
        }
    }

    @Deprecated
    public void swtichToNewTab() {
        switchToNewTab();
    }

    public void waitForPageToLoad() {
        try {
            wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        } catch (Exception e) {
            log.debug("Page readyState check completed or timed out: {}", e.getMessage());
        }
    }

    protected void click(By locator) {
        log.info("Clicking element: {}", locator);
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            scrollIntoView(element);
            element.click();
        } catch (Exception e) {
            log.warn("Standard click failed for {}, falling back to JS Click. Reason: {}", locator, e.getMessage());
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            jsClick(element);
        }
    }

    protected void jsClick(WebElement element) {
        scrollIntoView(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        } catch (Exception ignored) {
        }
    }

    protected void sendKeys(By locator, String text) {
        log.info("Typing '{}' into element: {}", text, locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        String text = element.getText().trim();
        log.info("Read text '{}' from element: {}", text, locator);
        return text;
    }

    protected boolean isDisplayed(By locator) {
        return isDisplayed(locator, defaultTimeoutSeconds);
    }

    protected boolean isDisplayed(By locator, int timeoutInSeconds) {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
            return shortWait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }

    protected void hover(By locator) {
        log.info("Hovering over element: {}", locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        new Actions(driver).moveToElement(element).perform();
    }
}
