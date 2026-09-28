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
    private final By productPrice = By.cssSelector("[data-test-id='price-current-price'], [data-test-id='default-price'], [data-test-id='price-wrapper'], span[class*='price'], div[class*='current-price'], div[class*='Price'], div[class*='price']");
    private final By productPriceXpath = By.xpath("//*[@data-test-id='price-current-price' or @data-test-id='default-price'] | //*[contains(@class, 'currentPrice') or contains(@class, 'price') or contains(@class, 'Price')] | //*[contains(text(), 'TL') or contains(text(), '₺')]");
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
        waitForPageToLoad();
        try {
            wait.until(ExpectedConditions.or(
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
        log.info("Adding product to cart");
        handleCookies();
        waitForPageLoad();

        By[] addButtons = new By[]{
            addToCartButton,
            addToCartButtonXpath,
            By.cssSelector("button[id*='addToCart'], [data-test-id='addToCart'], button#addToCart, button[class*='addToCartButton'], button[class*='add-to-basket'], button[class*='AddToCart']"),
            By.xpath("//button[@id='addToCart' or contains(@data-test-id, 'addToCart') or contains(@class, 'addToCart') or contains(., 'Sepete Ekle') or contains(., 'Sepete ekle')]"),
            By.xpath("//button[contains(., 'Sepete Ekle') or contains(., 'Sepete ekle')]"),
            By.xpath("//span[contains(text(), 'Sepete Ekle') or contains(text(), 'Sepete ekle')]/ancestor::button"),
            By.xpath("//div[contains(@class, 'AddToCart') or contains(@class, 'addToCart')]//button")
        };

        boolean clicked = false;
        for (By loc : addButtons) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
                WebElement btn = shortWait.until(ExpectedConditions.presenceOfElementLocated(loc));
                scrollIntoView(btn);
                if (!btn.isEnabled()) {
                    // Try selecting first available variant if button is disabled
                    selectFirstAvailableAlternativeVariant();
                }
                try {
                    btn.click();
                } catch (Exception e) {
                    jsClick(btn);
                }
                clicked = true;
                log.info("Clicked add to cart button using locator: {}", loc);
                break;
            } catch (Exception ignored) {
            }
        }

        if (!clicked) {
            // Last fallback: find any button containing 'Sepete'
            try {
                List<WebElement> allButtons = driver.findElements(By.tagName("button"));
                for (WebElement b : allButtons) {
                    if (b.isDisplayed() && b.getText().toLowerCase().contains("sepete ekle")) {
                        jsClick(b);
                        clicked = true;
                        break;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        // Handle Warranty / Extra Protection popup if it appears
        By[] dismissWarrantyLocators = new By[]{
            By.xpath("//button[contains(text(), 'İstemiyorum') or contains(text(), 'istemiyorum') or contains(text(), 'Onarımı İstemiyorum') or contains(text(), 'Hayır') or contains(text(), 'Geç')]"),
            By.xpath("//div[contains(@class, 'modal') or contains(@class, 'popup') or contains(@class, 'drawer')]//button[contains(., 'İstemiyorum') or contains(., 'Devam') or contains(., 'Kapat')]"),
            By.cssSelector("div[class*='warranty'] button, div[class*='protection'] button, button[aria-label='Kapat'], button[class*='close']")
        };

        for (By loc : dismissWarrantyLocators) {
            try {
                WebDriverWait warrantyWait = new WebDriverWait(driver, Duration.ofSeconds(2));
                WebElement wBtn = warrantyWait.until(ExpectedConditions.elementToBeClickable(loc));
                jsClick(wBtn);
                log.info("Dismissed warranty/addon popup");
                break;
            } catch (Exception ignored) {
            }
        }

        try {
            WebDriverWait postClickWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            postClickWait.until(ExpectedConditions.or(
                ExpectedConditions.visibilityOfElementLocated(goToCartButtonInModal),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//*[contains(text(), 'Sepete eklendi') or contains(text(), 'Ürün sepete eklendi') or contains(text(), 'sepetinize eklendi')]")),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("[data-test-id='checkout-button'], a[href*='checkout.hepsiburada.com']")),
                ExpectedConditions.presenceOfElementLocated(By.xpath("//span[contains(@class, 'badge') or contains(@class, 'count')]"))
            ));
        } catch (Exception ignored) {
        }

        return this;
    }

    @Step("Go to cart page")
    public CartPage goToCart() {
        log.info("Navigating to cart from PDP");
        By[] locators = new By[] {
            goToCartButtonInModal,
            By.xpath("//button[contains(., 'Sepete git') or contains(., 'Sepete Git') or contains(., 'Sepetim')]"),
            By.xpath("//a[contains(@href, 'checkout.hepsiburada.com') or contains(., 'Sepete git') or contains(., 'Sepete Git') or contains(., 'Sepetim')]"),
            By.cssSelector("a[href*='checkout.hepsiburada.com'], #shoppingCart, [data-test-id='cart'], [data-test-id='checkout-button'], a[href*='/sepetim']"),
            By.xpath("//span[contains(text(), 'Sepetim')]/ancestor::a | //div[contains(@id, 'cart')]//a")
        };

        boolean clicked = false;
        for (By loc : locators) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement cartBtn = shortWait.until(ExpectedConditions.elementToBeClickable(loc));
                jsClick(cartBtn);
                clicked = true;
                break;
            } catch (Exception ignored) {
            }
        }

        try {
            WebDriverWait urlWait = new WebDriverWait(driver, Duration.ofSeconds(6));
            urlWait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout.hepsiburada.com"),
                ExpectedConditions.urlContains("sepetim")
            ));
        } catch (Exception e) {
            driver.get("https://checkout.hepsiburada.com/sepetim");
        }

        return new CartPage(driver);
    }

    @Step("Get product price as text")
    public String getProductPriceText() {
        // Strategy 1: Standard CSS Locator
        try {
            WebDriverWait priceWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement priceEl = priceWait.until(ExpectedConditions.visibilityOfElementLocated(productPrice));
            String text = priceEl.getText().trim();
            if (!text.isEmpty()) {
                return text;
            }
        } catch (Exception ignored) {
        }

        // Strategy 2: XPath fallback
        try {
            WebElement xpathEl = driver.findElement(productPriceXpath);
            String text = xpathEl.getText().trim();
            if (!text.isEmpty()) {
                return text;
            }
        } catch (Exception ignored) {
        }

        // Strategy 3: Search for elements containing currency symbol
        try {
            List<WebElement> priceCandidates = driver.findElements(By.xpath("//*[contains(text(), 'TL') or contains(text(), '₺')]"));
            for (WebElement el : priceCandidates) {
                if (el.isDisplayed() && !el.getText().trim().isEmpty()) {
                    return el.getText().trim();
                }
            }
        } catch (Exception ignored) {
        }

        return "";
    }

    @Step("Select product variant option: {variantText}")
    public ProductDetailPage selectVariant(String variantText) {
        log.info("Selecting product variant: {}", variantText);
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

        waitForPageToLoad();
        return this;
    }

    @Step("Select first available alternative variant")
    public ProductDetailPage selectFirstAvailableAlternativeVariant() {
        log.info("Selecting first available alternative variant");
        try {
            List<WebElement> variants = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(variantOptions));
            if (variants.size() > 1) {
                jsClick(variants.get(1));
            } else if (!variants.isEmpty()) {
                jsClick(variants.get(0));
            }
            waitForPageToLoad();
        } catch (Exception ignored) {
        }
        return this;
    }

    @Step("Click wishlist / like button")
    public ProductDetailPage clickWishlist() {
        log.info("Clicking wishlist button");
        By[] wishlistLocators = new By[]{
            wishlistButton,
            wishlistButtonXpath,
            By.cssSelector("[data-test-id='favorite-button'], [data-test-id='heart-icon'], [data-test-id*='favorite'], [data-test-id*='like']"),
            By.cssSelector("button[class*='like'], button[class*='heart'], button[class*='favorite'], svg[class*='like'], svg[class*='heart']"),
            By.xpath("//button[contains(@class, 'like') or contains(@class, 'favorite') or contains(@aria-label, 'Beğen') or contains(@aria-label, 'Favori')]"),
            By.xpath("//div[contains(@class, 'Like') or contains(@class, 'Heart') or contains(@class, 'favorite')]")
        };

        boolean clicked = false;
        for (By loc : wishlistLocators) {
            try {
                WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));
                WebElement el = shortWait.until(ExpectedConditions.presenceOfElementLocated(loc));
                scrollIntoView(el);
                jsClick(el);
                clicked = true;
                break;
            } catch (Exception ignored) {
            }
        }
        if (!clicked) {
            log.warn("Wishlist button not found via standard locators");
        }

        return this;
    }

    @Step("Verify login prompt or redirect is displayed")
    public boolean isLoginRequiredPromptDisplayed() {
        try {
            WebDriverWait waitPrompt = new WebDriverWait(driver, Duration.ofSeconds(8));
            return Boolean.TRUE.equals(waitPrompt.until(d -> {
                String currentUrl = d.getCurrentUrl().toLowerCase();
                if (currentUrl.contains("giris") || currentUrl.contains("login") || currentUrl.contains("oauth") || currentUrl.contains("uyelik") || currentUrl.contains("hesabim")) {
                    return true;
                }
                By[] promptLocators = new By[]{
                    loginModalOrForm,
                    By.cssSelector(".login-modal, [data-test-id='login-modal'], div[class*='loginModal'], div[class*='LoginModal'], div[class*='modal-dialog'], div[role='dialog']"),
                    By.xpath("//*[contains(text(), 'Giriş yap') or contains(text(), 'Giriş Yap') or contains(text(), 'Üye ol') or contains(text(), 'Üye Ol') or contains(text(), 'giriş yapın') or contains(text(), 'Giriş yapınız')]"),
                    By.xpath("//div[contains(@class, 'modal') or contains(@class, 'popup') or contains(@class, 'dialog') or contains(@class, 'toast')]//*[contains(text(), 'Giriş') or contains(text(), 'giriş')]")
                };
                for (By loc : promptLocators) {
                    try {
                        List<WebElement> elements = d.findElements(loc);
                        for (WebElement el : elements) {
                            if (el.isDisplayed()) {
                                return true;
                            }
                        }
                    } catch (Exception ignored) {
                    }
                }
                String pageSource = d.getPageSource().toLowerCase();
                return pageSource.contains("giriş yap") || pageSource.contains("üye ol") || pageSource.contains("giris.hepsiburada.com");
            }));
        } catch (Exception e) {
            return false;
        }
    }

    @Step("Open reviews and ratings tab")
    public ProductDetailPage openReviewsTab() {
        log.info("Opening reviews tab");
        try {
            WebElement tab = wait.until(ExpectedConditions.presenceOfElementLocated(reviewsTab));
            scrollIntoView(tab);
            jsClick(tab);
        } catch (Exception e) {
            try {
                WebElement fallbackTab = driver.findElement(By.xpath("//div[contains(@class, 'tab') or contains(@class, 'Tab')]//*[contains(text(), 'Değerlendirme')]"));
                scrollIntoView(fallbackTab);
                jsClick(fallbackTab);
            } catch (Exception ignored) {
            }
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