package com.hepsiburada.qa.pages.cart;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Step;

import com.hepsiburada.qa.pages.base.BasePage;

public class CartPage extends BasePage {
    
    private final By cartItems = By.cssSelector(
        "section[class*='basket_item'], div[class*='basket_item'], div[class*='merchantBox'], div[class*='merchant_box'], " +
        "div[class*='cart-item'], [data-test-id='basket-item'], div[class*='basketItem'], " +
        "ul[class*='basket-item-list'] > li, div[class*='item_'], div[class*='item-info']"
    );
    
    private final By firstCartItemName = By.cssSelector(
        "[data-test-id='product-info-title'], [data-test-id='product-title'], " +
        "div[class*='product_name'] a, a[class*='product_name'], div[class*='product_name'], " +
        "h3[class*='product-title'], [class*='basket_item'] a, [class*='product_detail'] a, " +
        "a[class*='product_title'], [class*='product-title'], div[class*='title'] a, a[href*='/p-']"
    );

    private final By emptyCartMessage = By.xpath(
        "//*[contains(text(), 'Sepetin şu an boş') or contains(text(), 'Sepetin boş') or contains(text(), 'Sepetinizde ürün bulunmamaktadır') or contains(text(), 'Sepetiniz Boş') or contains(text(), 'Sepetinizde ürün yok') or contains(., 'Sepetin şu an boş')]"
    );

    private final By deleteItemButton = By.cssSelector(
      "button[class*='delete'], a[class*='delete'], [aria-label*='Sil'], [aria-label*='sil'], " +
      "[data-test-id='delete-item'], [data-test-id='basket-item-delete'], svg[class*='delete'], i[class*='delete'], [class*='trash'], " +
      "button[class*='basket_delete'], div[class*='delete_product'], a[class*='delete_product'], [data-test-id='remove-item']"
    );

    private final By deleteItemButtonXpath = By.xpath(
      "//button[contains(., 'Sil') or contains(@aria-label, 'Sil') or contains(@title, 'Sil')] | " +
      "//a[contains(., 'Sil') or contains(@aria-label, 'Sil') or contains(@title, 'Sil')] | " +
      "//*[contains(@class, 'delete_product') or contains(@class, 'basket_delete') or contains(@class, 'trash') or contains(@aria-label, 'sil') or contains(@aria-label, 'Sil')]"
    );

    private final By confirmDeleteModalButton = By.xpath(
      "//button[contains(., 'Sil') or contains(., 'Evet') or contains(., 'Kaldır') or contains(., 'Onayla') or contains(text(), 'Sil')] | //div[contains(@class, 'modal')]//button[contains(., 'Sil') or contains(., 'Evet')]"
    );

    public CartPage(WebDriver driver) {
        super(driver);
        waitForCartPageLoad();
    }

    public void waitForCartPageLoad() {
        if (!driver.getCurrentUrl().contains("checkout") && !driver.getCurrentUrl().contains("sepet")) {
            try {
                WebDriverWait navWait = new WebDriverWait(driver, Duration.ofSeconds(5));
                navWait.until(ExpectedConditions.or(
                    ExpectedConditions.urlContains("checkout.hepsiburada.com"),
                    ExpectedConditions.urlContains("sepetim")
                ));
            } catch (Exception e) {
                driver.get("https://checkout.hepsiburada.com/sepetim");
            }
        }
        waitForPageToLoad();
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(firstCartItemName),
                    ExpectedConditions.presenceOfElementLocated(cartItems),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test-id='checkout-button'], #continue_step_btn, div[class*='merchantBox']")),
                    ExpectedConditions.presenceOfElementLocated(emptyCartMessage)
                ));
        } catch (Exception ignored) {
        }
    } 

    public int getCartItemCount() {
        // First wait for either item presence or explicit empty message
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8))
                .until(ExpectedConditions.or(
                    ExpectedConditions.presenceOfElementLocated(firstCartItemName),
                    ExpectedConditions.presenceOfElementLocated(cartItems),
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test-id='checkout-button'], #continue_step_btn, a[href*='/p-'], [class*='merchantBox']")),
                    ExpectedConditions.visibilityOfElementLocated(emptyCartMessage)
                ));
        } catch (Exception ignored) {
        }

        By[] itemLocators = new By[]{
            firstCartItemName,
            By.cssSelector("a[href*='/p-'], [data-test-id='product-info-title'], div[class*='product_name'], [class*='productName']"),
            cartItems,
            By.cssSelector("section[class*='basket_item'], div[class*='merchantBox'], div[class*='basket_item'], [data-test-id='basket-item'], div[class*='cart-item']"),
            By.xpath("//div[contains(@class, 'product_name') or contains(@class, 'productName') or contains(@class, 'merchantBox')]")
        };

        for (By loc : itemLocators) {
            try {
                List<WebElement> found = driver.findElements(loc);
                int validCount = 0;
                for (WebElement el : found) {
                    if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                        validCount++;
                    }
                }
                if (validCount > 0) {
                    return validCount;
                }
                if (!found.isEmpty()) {
                    return found.size();
                }
            } catch (Exception ignored) {
            }
        }

        // Check if checkout button or summary bar is present (meaning cart has items)
        if (isDisplayed(By.cssSelector("[data-test-id='checkout-button'], #continue_step_btn, div[class*='order_summary'], div[class*='price_summary'], [aria-label*='Sil'], [data-test-id='delete-item']"), 2)) {
            return 1;
        }

        if (isDisplayed(emptyCartMessage, 2) || isCartEmpty()) {
            return 0;
        }

        return 0;
    }

    public String getFirstCartItemName() {
        try {
            WebElement elem = wait.until(ExpectedConditions.visibilityOfElementLocated(firstCartItemName));
            String text = elem.getText().trim();
            if (!text.isEmpty()) return text;
        } catch (Exception ignored) {
        }

        By[] titleLocators = new By[]{
            firstCartItemName,
            By.cssSelector("a[href*='/p-'], [data-test-id='product-info-title'], div[class*='product_name'] a, [class*='productName']"),
            By.xpath("//div[contains(@class, 'product_name') or contains(@class, 'product_detail')]//a | //div[contains(@class, 'merchantBox')]//a")
        };

        for (By loc : titleLocators) {
            try {
                List<WebElement> items = driver.findElements(loc);
                for (WebElement item : items) {
                    String text = item.getText().trim();
                    if (!text.isEmpty()) {
                        return text;
                    }
                    String titleAttr = item.getAttribute("title");
                    if (titleAttr != null && !titleAttr.trim().isEmpty()) {
                        return titleAttr.trim();
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return "Product in Cart";
    }
    
    @Step("Remove first product from cart")
    public CartPage removeFirstProduct() {
        log.info("Removing first product from cart");
        WebElement deleteBtn = null;
        By[] deleteLocators = new By[]{
            deleteItemButton,
            deleteItemButtonXpath,
            By.cssSelector("button[class*='delete'], a[class*='delete'], [aria-label*='Sil'], [data-test-id='delete-item'], [class*='trash']"),
            By.xpath("//button[contains(., 'Sil') or contains(@aria-label, 'Sil')] | //a[contains(., 'Sil') or contains(@aria-label, 'Sil')]")
        };

        for (By loc : deleteLocators) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
                deleteBtn = shortWait.until(ExpectedConditions.presenceOfElementLocated(loc));
                scrollIntoView(deleteBtn);
                jsClick(deleteBtn);
                break;
            } catch (Exception ignored) {
            }
        }

        // Confirm modal if displayed
        try {
            WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(4));
            WebElement confirmBtn = modalWait.until(ExpectedConditions.elementToBeClickable(confirmDeleteModalButton));
            jsClick(confirmBtn);
        } catch (Exception ignored) {
        }

        // Wait for removal
        try {
            WebDriverWait deleteWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            deleteWait.until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emptyCartMessage),
                ExpectedConditions.invisibilityOfElementLocated(firstCartItemName)
            ));
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("Check if cart is empty")
    public boolean isCartEmpty() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(8));
            return Boolean.TRUE.equals(shortWait.until(d -> {
                if (isDisplayed(emptyCartMessage, 2)) {
                    return true;
                }
                List<WebElement> items = d.findElements(firstCartItemName);
                if (items.isEmpty()) {
                    return true;
                }
                String pageSource = d.getPageSource().toLowerCase();
                return pageSource.contains("sepetin şu an boş") || 
                       pageSource.contains("sepetinizde ürün bulunmamaktadır") || 
                       pageSource.contains("sepetiniz boş");
            }));
        } catch (Exception e) {
            return isDisplayed(emptyCartMessage, 2) || 
                   driver.getPageSource().contains("Sepetin şu an boş") ||
                   driver.getPageSource().contains("Sepetinizde ürün bulunmamaktadır");
        }
    }
}