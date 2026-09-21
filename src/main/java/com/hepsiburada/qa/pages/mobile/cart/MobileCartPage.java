package com.hepsiburada.qa.pages.mobile.cart;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.MobileBasePage;

import io.appium.java_client.AppiumDriver;
import io.qameta.allure.Step;

public class MobileCartPage extends MobileBasePage {

    private final By cartItems = By.cssSelector(
        "[class*='merchantBox'], [class*='basket_item'], [class*='cart-item'], [class*='basketItem'], " +
        "div[class*='product_name'], a[class*='product_name'], [data-test-id='product-info-title'], " +
        "div[class*='product_detail'], div[class*='product_container'], section[class*='cart-item'], " +
        "ul[class*='productListContent'] > li, div[class*='product_info'], div[class*='item_'], div[class*='cartItem']"
    );

    private final By firstCartItemName = By.cssSelector(
        "div[class*='product_name'] a, a[class*='product_name'], [data-test-id='product-info-title'], " +
        "div[class*='product_name'], h3[class*='product-title'], [class*='basket_item'] a, [class*='product_detail'] a, " +
        "a[class*='product_title'], [class*='product-title']"
    );

    private final By emptyCartMessage = By.xpath(
        "//*[contains(text(), 'Sepetin şu an boş') or contains(text(), 'Sepetin boş') or contains(text(), 'Sepetinizde ürün bulunmamaktadır') or contains(text(), 'Sepetiniz Boş') or contains(text(), 'şu an boş') or contains(., 'Sepetin şu an boş')]"
    );

    private final By deleteItemButton = By.cssSelector(
        "button[class*='delete'], a[class*='delete'], [aria-label*='Sil'], [aria-label*='sil'], " +
        "[data-test-id='delete-item'], svg[class*='delete'], i[class*='delete'], [class*='trash'], " +
        "button[class*='basket_delete'], div[class*='delete_product']"
    );

    private final By deleteItemButtonXpath = By.xpath(
        "//button[contains(., 'Sil') or contains(@aria-label, 'Sil')] | " +
        "//a[contains(., 'Sil') or contains(@aria-label, 'Sil')] | " +
        "//*[contains(@class, 'delete_product') or contains(@class, 'basket_delete') or contains(@class, 'trash')]"
    );

    private final By confirmDeleteModalButton = By.xpath(
        "//button[contains(., 'Sil') or contains(., 'Evet') or contains(., 'Kaldır') or contains(., 'Onayla')]"
    );

    public MobileCartPage(AppiumDriver driver) {
        super(driver);
        waitForCartPageLoad();
    }

    public void waitForCartPageLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(cartItems),
                    ExpectedConditions.presenceOfElementLocated(firstCartItemName),
                    ExpectedConditions.urlContains("checkout.hepsiburada.com"),
                    ExpectedConditions.urlContains("sepetim"),
                    ExpectedConditions.presenceOfElementLocated(emptyCartMessage)
                ));
        } catch (Exception ignored) {
        }
    }

    @Step("Get cart item count on mobile")
    public int getCartItemCount() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(cartItems),
                    ExpectedConditions.presenceOfElementLocated(firstCartItemName)
                ));
        } catch (Exception ignored) {
        }

        try {
            List<WebElement> itemTitles = driver.findElements(firstCartItemName);
            if (!itemTitles.isEmpty()) {
                return itemTitles.size();
            }
            List<WebElement> items = driver.findElements(cartItems);
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Step("Get first cart item name on mobile")
    public String getFirstCartItemName() {
        try {
            WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(firstCartItemName));
            return elem.getText().trim();
        } catch (Exception e) {
            try {
                List<WebElement> items = driver.findElements(firstCartItemName);
                if (!items.isEmpty()) {
                    return items.get(0).getText().trim();
                }
            } catch (Exception ignored) {
            }
            return "";
        }
    }

    @Step("Remove first product from cart on mobile")
    public MobileCartPage removeFirstProduct() {
        log.info("Removing first product from mobile cart");
        WebElement deleteBtn = null;
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            try {
                deleteBtn = shortWait.until(ExpectedConditions.elementToBeClickable(deleteItemButton));
            } catch (Exception e) {
                deleteBtn = shortWait.until(ExpectedConditions.elementToBeClickable(deleteItemButtonXpath));
            }
            jsClick(deleteBtn);
        } catch (Exception e) {
            try {
                deleteBtn = driver.findElement(deleteItemButtonXpath);
                jsClick(deleteBtn);
            } catch (Exception ex) {
                log.warn("Could not find delete button on mobile cart: {}", ex.getMessage());
            }
        }

        try {
            WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement confirmBtn = modalWait.until(ExpectedConditions.elementToBeClickable(confirmDeleteModalButton));
            jsClick(confirmBtn);
        } catch (Exception ignored) {
        }

        try {
            WebDriverWait deleteWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            deleteWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emptyCartMessage),
                ExpectedConditions.invisibilityOfElementLocated(firstCartItemName)
            ));
        } catch (Exception ignored) {
        }

        return this;
    }

    @Step("Check if cart is empty on mobile")
    public boolean isCartEmpty() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            return shortWait.until(d -> {
                if (isDisplayed(emptyCartMessage)) {
                    return true;
                }
                String pageSource = driver.getPageSource();
                return pageSource.contains("Sepetin şu an boş") || pageSource.contains("Sepetinizde ürün bulunmamaktadır");
            });
        } catch (Exception e) {
            return isDisplayed(emptyCartMessage) || driver.getPageSource().contains("Sepetin şu an boş");
        }
    }
}
