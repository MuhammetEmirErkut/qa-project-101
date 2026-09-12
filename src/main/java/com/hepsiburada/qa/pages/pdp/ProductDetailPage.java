package com.hepsiburada.qa.pages.pdp;

import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;
import com.hepsiburada.qa.pages.cart.CartPage;
import io.qameta.allure.Step;

public class ProductDetailPage extends BasePage {

    private final By productTitle = By.cssSelector("h1#product-name, [data-test-id='title'], h1[class*='product-name'], h1[class*='title'], h1");
    private final By addToCartButton = By.cssSelector("button[id*='addToCart'], [data-test-id='addToCart'], button#addToCart, button[class*='addToCartButton'], button[class*='add-to-basket']");
    private final By addToCartButtonXpath = By.xpath("//button[@id='addToCart' or contains(@data-test-id, 'addToCart') or contains(@class, 'addToCart') or contains(., 'Sepete Ekle') or contains(., 'Sepete ekle')]");
    private final By goToCartButtonInModal = By.xpath("//button[contains(., 'Sepete git') or contains(., 'Sepete Git') or contains(., 'Sepetim')] | //a[contains(., 'Sepete git') or contains(., 'Sepete Git') or contains(., 'Sepetim') or contains(@href, 'checkout.hepsiburada.com')]");
    private final By productPrice = By.cssSelector("[data-test-id='price-current-price'], [data-test-id='default-price'], span[class*='price'], div[class*='current-price']");
    private final By variantOptions = By.cssSelector("div[class*='variant'] button, div[class*='variant'] a, [data-test-id*='variant'], a[class*='variantBox'], button[class*='variantBox']");
    private final By wishlistButton = By.cssSelector("button[class*='like'], button[class*='heart'], button[class*='favorite'], [data-test-id*='favorite'], [data-test-id*='like'], svg[class*='like']");
    private final By wishlistButtonXpath = By.xpath("//button[contains(@class, 'like') or contains(@class, 'favorite') or contains(@aria-label, 'Beğen') or contains(@aria-label, 'Favori')] | //*[@data-test-id='favorite' or @data-test-id='like-button'] | //div[contains(@class, 'Heart')] | //*[contains(@class, 'customerAccount-Like')]");
    private final By loginModalOrForm = By.cssSelector("#login, .login-modal, [data-test-id='login-form'], input[type='email'], input#txtUserName, form[action*='login'], div[class*='login']");
    private final By reviewsTab = By.xpath(
        "//*[@id='reviewsTab' or @data-test-id='tab-reviews'] | " +
        "//div[contains(@class, 'tab') or contains(@class, 'Tab')]//*[contains(text(), 'Değerlendirme') or contains(text(), 'Yorumlar')] | " +
        "//a[starts-with(@href, '#') and (contains(., 'Değerlendirme') or contains(., 'Yorum'))] | " +
        "//a[contains(@href, '-yorumlari') or contains(@href, 'degerlendirmeler')] | " +
        "//div[contains(@class, 'product') or contains(@class, 'pdp')]//*[contains(text(), 'Değerlendirmeler')]"
    );
    private final By reviewsContentContainer = By.cssSelector("#reviews, #tab-reviews, [data-test-id='reviews-container'], div[class*='reviewsContainer'], div[class*='rating-wrapper'], div[class*='hermes-ReviewCard'], div[class*='ReviewCard']");
    private final By ratingScoreElement = By.cssSelector("[data-test-id='rating-score'], div[class*='rating-score'], span[class*='ratingStar'], div[class*='rating']");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(productTitle),
                ExpectedConditions.presenceOfElementLocated(addToCartButton),
                ExpectedConditions.presenceOfElementLocated(addToCartButtonXpath)
            ));
        } catch (Exception ignored) {
        }
    }

    @Step("Get product name from product detail page")
    public String getProductName() {
        try {
            return getText(productTitle).trim();
        } catch (Exception e) {
            try {
                return driver.findElement(productTitle).getText().trim();
            } catch (Exception ex) {
                return driver.getTitle();
            }
        }
    }

    @Step("Add product to cart")
    public ProductDetailPage addToCart() {
        WebElement btn = null;
        try {
            btn = wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        } catch (Exception e) {
            try {
                btn = wait.until(ExpectedConditions.elementToBeClickable(addToCartButtonXpath));
            } catch (Exception ex) {
                btn = wait.until(ExpectedConditions.presenceOfElementLocated(addToCartButtonXpath));
            }
        }

        try {
            jsClick(btn);
        } catch (Exception e) {
            btn.click();
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    @Step("Go to cart page")
    public CartPage goToCart() {
        By[] locators = new By[] {
            goToCartButtonInModal,
            By.xpath("//button[contains(., 'Sepete git') or contains(., 'Sepete Git') or contains(., 'Sepetim')]"),
            By.xpath("//a[contains(@href, 'checkout.hepsiburada.com') or contains(., 'Sepete git') or contains(., 'Sepete Git')]"),
            By.cssSelector("a[href*='checkout.hepsiburada.com'], #shoppingCart, [data-test-id='cart']")
        };

        boolean navigated = false;
        for (By loc : locators) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement cartBtn = shortWait.until(ExpectedConditions.elementToBeClickable(loc));
                jsClick(cartBtn);
                navigated = true;
                break;
            } catch (Exception ignored) {
            }
        }

        if (!navigated) {
            driver.get("https://checkout.hepsiburada.com/sepetim");
        }

        return new CartPage(driver);
    }

    @Step("Get product price as text")
    public String getProductPriceText() {
        try {
            WebElement priceEl = wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice));
            return priceEl.getText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    @Step("Select product variant option: {variantText}")
    public ProductDetailPage selectVariant(String variantText) {
        By specificVariant = By.xpath(
            "//button[contains(., '" + variantText + "')] | " +
            "//a[contains(., '" + variantText + "')] | " +
            "//div[contains(@class, 'variant')]//*[contains(text(), '" + variantText + "')]"
        );

        try {
            WebElement variantEl = wait.until(ExpectedConditions.elementToBeClickable(specificVariant));
            jsClick(variantEl);
        } catch (Exception e) {
            List<WebElement> variants = driver.findElements(variantOptions);
            for (WebElement v : variants) {
                if (v.getText().contains(variantText)) {
                    jsClick(v);
                    break;
                }
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    @Step("Select first available alternative variant")
    public ProductDetailPage selectFirstAvailableAlternativeVariant() {
        try {
            List<WebElement> variants = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(variantOptions));
            if (variants.size() > 1) {
                jsClick(variants.get(1));
            } else if (!variants.isEmpty()) {
                jsClick(variants.get(0));
            }
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("Click wishlist / like button")
    public ProductDetailPage clickWishlist() {
        WebElement likeBtn = null;
        try {
            likeBtn = wait.until(ExpectedConditions.elementToBeClickable(wishlistButton));
        } catch (Exception e) {
            try {
                likeBtn = wait.until(ExpectedConditions.elementToBeClickable(wishlistButtonXpath));
            } catch (Exception ex) {
                likeBtn = wait.until(ExpectedConditions.presenceOfElementLocated(wishlistButtonXpath));
            }
        }

        try {
            jsClick(likeBtn);
        } catch (Exception e) {
            likeBtn.click();
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    @Step("Verify login prompt or redirect is displayed")
    public boolean isLoginRequiredPromptDisplayed() {
        String currentUrl = driver.getCurrentUrl().toLowerCase();
        if (currentUrl.contains("giris") || currentUrl.contains("login") || currentUrl.contains("oauth")) {
            return true;
        }

        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Boolean isDisplayed = shortWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(loginModalOrForm),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Giriş yap') or contains(text(), 'Üye ol')]"))
            ));
            return Boolean.TRUE.equals(isDisplayed);
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Open reviews and ratings tab")
    public ProductDetailPage openReviewsTab() {
        try {
            WebElement tab = wait.until(ExpectedConditions.presenceOfElementLocated(reviewsTab));
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", tab);
            Thread.sleep(600);
            jsClick(tab);
        } catch (Exception e) {
            try {
                WebElement fallbackTab = driver.findElement(By.xpath("//div[contains(@class, 'tab') or contains(@class, 'Tab')]//*[contains(text(), 'Değerlendirme')]"));
                ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", fallbackTab);
                jsClick(fallbackTab);
            } catch (Exception ignored) {
            }
        }

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {
        }

        return this;
    }

    @Step("Check if reviews or ratings container is visible")
    public boolean isReviewsContainerVisible() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            Boolean isVisible = shortWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(reviewsContentContainer),
                ExpectedConditions.visibilityOfElementLocated(ratingScoreElement),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//div[contains(@class, 'rating') or contains(@class, 'Review') or contains(@id, 'review')]")),
                ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Değerlendirme') and contains(text(), 'Puan')]"))
            ));
            return Boolean.TRUE.equals(isVisible);
        } catch (Exception e) {
            return false;
        }
    }
}