package com.hepsiburada.qa.pages.base;

import java.time.Duration;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.AppiumDriver;

public abstract class MobileBasePage {

    protected static final Logger log = LogManager.getLogger(MobileBasePage.class);

    protected AppiumDriver driver;
    protected WebDriverWait wait;
    protected int defaultTimeoutSeconds;

    public MobileBasePage(AppiumDriver driver) {
        this(driver, 15);
    }

    public MobileBasePage(AppiumDriver driver, int timeoutInSeconds) {
        this.driver = driver;
        this.defaultTimeoutSeconds = timeoutInSeconds;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    protected void click(By locator) {
        log.info("Mobile click on element: {}", locator);
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            scrollIntoView(element);
            element.click();
        } catch (Exception e) {
            log.warn("Standard click failed for mobile locator {}, falling back to JS Click. Error: {}", locator, e.getMessage());
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            jsClick(element);
        }
    }

    protected void jsClick(WebElement element) {
        scrollIntoView(element);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    protected void sendKeys(By locator, String text) {
        log.info("Mobile typing '{}' into element: {}", text, locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected String getText(By locator) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        String text = element.getText().trim();
        log.info("Read mobile text '{}' from element: {}", text, locator);
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

    protected void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        } catch (Exception ignored) {
        }
    }

    public void waitForPageToLoad() {
        try {
            wait.until(d -> "complete".equals(((JavascriptExecutor) d).executeScript("return document.readyState")));
        } catch (Exception e) {
            log.debug("Mobile page readyState check timeout: {}", e.getMessage());
        }
    }

    public void swipeDown() {
        try {
            Dimension size = driver.manage().window().getSize();
            int startX = size.width / 2;
            int startY = (int) (size.height * 0.70);
            int endY = (int) (size.height * 0.30);

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
            Sequence swipe = new Sequence(finger, 1);
            swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
            swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
            swipe.addAction(new Pause(finger, Duration.ofMillis(100)));
            swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), startX, endY));
            swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

            driver.perform(Collections.singletonList(swipe));
        } catch (Exception e) {
            log.warn("Swipe gesture fallback to JS scroll. Error: {}", e.getMessage());
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, 500);");
        }
    }
}
