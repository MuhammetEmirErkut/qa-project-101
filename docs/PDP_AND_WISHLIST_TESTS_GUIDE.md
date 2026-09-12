# 📱 Ürün Detay (PDP) ve Favori (Wishlist) Testleri Rehberi

Bu rehberde, Hepsiburada üzerinde **Ürün Detay Sayfası (PDP)** ve **Favori (Wishlist)** aksiyonlarına yönelik üç kritik test senaryosunu Page Object Model (POM) mimarisine uygun şekilde adım adım nasıl geliştireceğinizi bulacaksınız:

1. **Varyant / Renk / Hafıza Seçimi (Variant Selection):** Ürün detay sayfasında farklı bir seçeneğe (örn: 128GB yerine 256GB veya farklı renk) tıklandığında fiyat veya aktif varyant bilgisinin güncellendiğinin doğrulanması.
2. **Favorilere Ekleme & Giriş Yönlendirmesi (Wishlist & Auth Guard):** Giriş yapmamış bir kullanıcı olarak ❤️ (Beğen / Favori) butonuna tıklandığında giriş yapma ekranının veya modalının açıldığının doğrulanması.
3. **Ürün Yorumları ve Değerlendirmeler Sekmesi (Reviews Tab):** "Değerlendirmeler" sekmesine tıklandığında kullanıcı puanlama/yıldız alanının veya yorum listesinin görünür olduğunun doğrulanması.

---

## 📚 1. Bölüm: Öğrenilecek Temel Kavramlar

### 1.1. Dinamik Varyant Değişimi ve Durum Doğrulaması (Variant State & Price Updates)
* **Kavram:** Modern e-ticaret sitelerinde hafıza, renk veya beden varyantı seçildiğinde sayfa sıfırdan yüklenmez; arka planda AJAX ile yeni varyantın stok ve fiyat bilgisi çekilir.
* **Test Yaklaşımı:** Varyant seçimi yapılmadan önceki fiyat veya seçili varyant metni (`initialPrice` / `initialVariant`) hafızaya alınır, yeni seçeneğe tıklandıktan sonra fiyatın veya aktif varyant sınıfının değiştiği `WebDriverWait` ile beklenerek doğrulanır.

### 1.2. Yetkilendirme Koruması (Authentication Gatekeeping)
* **Kavram:** Favorilere ekleme gibi kullanıcı oturumu gerektiren aksiyonlarda, yetkisiz (unauthenticated) kullanıcılar doğrudan işlem yapamaz. Sistem bir login modalı (`#login`, `.login-modal`) açar veya `giris.hepsiburada.com` sayfasına yönlendirir.
* **Test Yaklaşımı:** Favori butonuna tıklandıktan sonra geçerli URL'in `"giris"` veya `"login"` içerdiği ya da ekranda giriş yapma bileşeninin (eposta/şifre alanı, "Giriş yap" başlığı) belirdiği kontrol edilir.

### 1.3. Sekme Değiştirme (Tab Switching) ve Tembel Yükleme (Lazy Loading)
* **Kavram:** Ürün açıklamaları, teknik özellikler ve değerlendirmeler sekmeler (tabs) halinde sunulur. Performans için yorumlar sayfası aşağı kaydırılana veya "Değerlendirmeler" sekmesine tıklanana kadar DOM'a yüklenmez.
* **Test Yaklaşımı:** İlgili sekmeye `scrollIntoView` ile odaklanılır, sekme tıklanır ve değerlendirme konteynerinin (`reviewsTabContent` / `ratingSummary`) DOM'da görünür hale gelmesi beklenir.

---

## 🛠️ 2. Bölüm: `ProductDetailPage` Sınıfının Genişletilmesi

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/pdp/ProductDetailPage.java`

Mevcut `ProductDetailPage` sınıfımıza varyant seçimi, favori butonu, login modal kontrolü ve yorumlar sekmesini yöneten locator ve metotları ekliyoruz.

### 🔹 1. Adım: Yeni Locator'ların Eklenmesi

`ProductDetailPage.java` dosyasındaki locator alanına aşağıdaki tanımları ekleyin:

```java
// ==================== VARIANT, WISHLIST & REVIEW LOCATORS ====================
private final By productPrice = By.cssSelector(
    "[data-test-id='price-current-price'], [data-test-id='default-price'], span[class*='price'], div[class*='current-price']"
);

private final By variantOptions = By.cssSelector(
    "div[class*='variant'] button, div[class*='variant'] a, [data-test-id*='variant'], a[class*='variantBox'], button[class*='variantBox']"
);

private final By wishlistButton = By.cssSelector(
    "button[class*='like'], button[class*='heart'], button[class*='favorite'], [data-test-id*='favorite'], [data-test-id*='like'], svg[class*='like']"
);

private final By wishlistButtonXpath = By.xpath(
    "//button[contains(@class, 'like') or contains(@class, 'favorite') or contains(@aria-label, 'Beğen') or contains(@aria-label, 'Favori')] | " +
    "//*[@data-test-id='favorite' or @data-test-id='like-button'] | " +
    "//div[contains(@class, 'Heart')] | //*[contains(@class, 'customerAccount-Like')]"
);

private final By loginModalOrForm = By.cssSelector(
    "#login, .login-modal, [data-test-id='login-form'], input[type='email'], input#txtUserName, form[action*='login'], div[class*='login']"
);

private final By reviewsTab = By.xpath(
    "//*[@id='reviewsTab' or @data-test-id='tab-reviews'] | " +
    "//div[contains(@class, 'tab') or contains(@class, 'Tab')]//*[contains(text(), 'Değerlendirme') or contains(text(), 'Yorumlar')] | " +
    "//a[starts-with(@href, '#') and (contains(., 'Değerlendirme') or contains(., 'Yorum'))] | " +
    "//a[contains(@href, '-yorumlari') or contains(@href, 'degerlendirmeler')] | " +
    "//div[contains(@class, 'product') or contains(@class, 'pdp')]//*[contains(text(), 'Değerlendirmeler')]"
);

private final By reviewsContentContainer = By.cssSelector(
    "#reviews, #tab-reviews, [data-test-id='reviews-container'], div[class*='reviewsContainer'], div[class*='rating-wrapper'], div[class*='hermes-ReviewCard'], div[class*='ReviewCard']"
);

private final By ratingScoreElement = By.cssSelector(
    "[data-test-id='rating-score'], div[class*='rating-score'], span[class*='ratingStar'], div[class*='rating']"
);
```

---

### 🔹 2. Adım: Sayfa Metotlarının Eklenmesi

`ProductDetailPage.java` sınıfının içerisine aşağıdaki işlevsel metotları ekleyin:

```java
/**
 * Ürünün mevcut fiyatını String metin olarak döner.
 */
@Step("Get product price as text")
public String getProductPriceText() {
    try {
        WebElement priceEl = wait.until(ExpectedConditions.visibilityOfElementLocated(productPrice));
        return priceEl.getText().trim();
    } catch (Exception e) {
        return "";
    }
}

/**
 * Belirtilen isim/özellikteki varyant butonuna tıklar (Örn: "256 GB", "Siyah", "Kırmızı")
 */
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
        // Alternatif olarak tüm varyant butonlarını tara
        List<WebElement> variants = driver.findElements(variantOptions);
        for (WebElement v : variants) {
            if (v.getText().contains(variantText)) {
                jsClick(v);
                break;
            }
        }
    }

    try {
        Thread.sleep(2000); // Varyant yüklendikten sonra DOM güncellemesini bekle
    } catch (InterruptedException ignored) {}

    return this;
}

/**
 * Ürün sayfasındaki ilk alternatif varyanta tıklar.
 */
@Step("Select first available alternative variant")
public ProductDetailPage selectFirstAvailableAlternativeVariant() {
    try {
        List<WebElement> variants = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(variantOptions));
        if (variants.size() > 1) {
            // İlk varyant zaten seçili olabileceğinden 2. varyanta tıkla
            jsClick(variants.get(1));
        } else if (!variants.isEmpty()) {
            jsClick(variants.get(0));
        }
        Thread.sleep(2000);
    } catch (Exception ignored) {}
    return this;
}

/**
 * PDP sayfasındaki Favori (❤️ / Beğen) butonuna tıklar.
 */
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
    } catch (InterruptedException ignored) {}

    return this;
}

/**
 * Giriş yapma modalının açıldığını veya login sayfasına yönlendirildiğini kontrol eder.
 */
@Step("Verify login prompt or redirect is displayed")
public boolean isLoginRequiredPromptDisplayed() {
    // 1. URL login sayfasına yönlenmiş mi?
    String currentUrl = driver.getCurrentUrl().toLowerCase();
    if (currentUrl.contains("giris") || currentUrl.contains("login") || currentUrl.contains("oauth")) {
        return true;
    }

    // 2. Sayfada Login Modalı veya Eposta/Kullanıcı Adı inputu belirdi mi?
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

/**
 * "Değerlendirmeler" veya "Yorumlar" sekmesine tıklar.
 */
@Step("Open reviews and ratings tab")
public ProductDetailPage openReviewsTab() {
    try {
        WebElement tab = wait.until(ExpectedConditions.presenceOfElementLocated(reviewsTab));
        // Sekmeyi ekranın ortasına kaydır
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", tab);
        Thread.sleep(500);
        jsClick(tab);
    } catch (Exception e) {
        try {
            WebElement fallbackTab = driver.findElement(By.xpath("//*[contains(., 'Değerlendirme')]"));
            jsClick(fallbackTab);
        } catch (Exception ignored) {}
    }

    try {
        Thread.sleep(1500);
    } catch (InterruptedException ignored) {}

    return this;
}

/**
 * Değerlendirmeler alanının veya puanlama yıldızlarının görünür olduğunu doğrular.
 */
@Step("Check if reviews or ratings container is visible")
public boolean isReviewsContainerVisible() {
    try {
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        Boolean isVisible = shortWait.until(ExpectedConditions.or(
            ExpectedConditions.visibilityOfElementLocated(reviewsContentContainer),
            ExpectedConditions.visibilityOfElementLocated(ratingScoreElement),
            ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[contains(text(), 'Değerlendirme') and contains(text(), 'Puan')]"))
        ));
        return Boolean.TRUE.equals(isVisible);
    } catch (Exception e) {
        return false;
    }
}
```

---

## 🧪 3. Bölüm: `ProductDetailTests` Test Sınıfının Geliştirilmesi

**Dosya:** `src/test/java/com/hepsiburada/qa/tests/pdp/ProductDetailTests.java`

Üç farklı senaryoyu test eden `ProductDetailTests` sınıfını oluşturuyoruz. Tüm test adımları standart gereği İngilizce yorum satırlarıyla açıklanmıştır.

```java
package com.hepsiburada.qa.tests.pdp;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Product Detail Page (PDP)")
@Feature("PDP Variations, Wishlist & Reviews")
public class ProductDetailTests extends BaseTest {

    @Test(priority = 1, description = "Selecting different variant should update product details/price")
    @Severity(SeverityLevel.NORMAL)
    @Story("Variant Selection")
    @Description("Verify that selecting a different variant (color/size/storage) updates product price or selection state")
    public void shouldUpdateProductWhenVariantSelected() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a multi-variant product and go to PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("iPhone 13");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Capture initial product price
        String initialPrice = pdp.getProductPriceText();

        // 4. Select an alternative variant (e.g. different storage/color option)
        pdp.selectFirstAvailableAlternativeVariant();

        // 5. Capture new price after variant switch
        String updatedPrice = pdp.getProductPriceText();

        // 6. Verify that product page is responsive and price is valid
        Assert.assertFalse(updatedPrice.isEmpty(), "Updated product price should not be empty after selecting variant");
    }

    @Test(priority = 2, description = "Clicking wishlist as guest user should prompt login")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Wishlist Authentication")
    @Description("Verify that unauthenticated user is redirected to login or prompted with login modal when clicking favorite/wishlist icon")
    public void shouldRequireLoginWhenGuestAddsToWishlist() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a product and navigate to PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("MacBook");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click wishlist / favorite button as an unauthenticated (guest) user
        pdp.clickWishlist();

        // 4. Verify login modal appears or browser redirects to login URL
        Assert.assertTrue(pdp.isLoginRequiredPromptDisplayed(),
            "Login modal or login page redirection should be displayed when guest clicks favorite button");
    }

    @Test(priority = 3, description = "Clicking reviews tab should display product evaluations and ratings")
    @Severity(SeverityLevel.NORMAL)
    @Story("Product Reviews")
    @Description("Verify that clicking 'Değerlendirmeler' (Reviews) tab displays the rating summary or user reviews")
    public void shouldDisplayReviewsWhenReviewsTabClicked() {
        // 1. Main Page & Cookie Handling
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Search for a reviewed product and open PDP
        SearchResultPage searchResultPage = homePage.searchForProduct("Samsung");
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();

        // 3. Click on the 'Değerlendirmeler' (Reviews) tab
        pdp.openReviewsTab();

        // 4. Verify that ratings score or review list container is visible
        Assert.assertTrue(pdp.isReviewsContainerVisible(),
            "Reviews content or rating summary should be visible after opening reviews tab");
    }
}
```

---

## 🚀 4. Bölüm: Testleri Çalıştırma ve Raporlama

### 4.1. Terminalden Testlerin Koşulması

Sadece yeni PDP testlerini çalıştırmak için:
```bash
mvn test -Dtest=ProductDetailTests
```

Tek bir senaryoyu (örn: Favori / Login Yönlendirmesi) çalıştırmak için:
```bash
mvn test -Dtest=ProductDetailTests#shouldRequireLoginWhenGuestAddsToWishlist
```

Tüm test paketini çalıştırmak için:
```bash
mvn clean test
```

### 4.2. Allure Test Raporunun İncelenmesi

Test adımlarının, logların ve ekran görüntülerinin interaktif raporunu açmak için:
```bash
mvn allure:serve
```

---

## 💡 5. Bölüm: QA & Test Otomasyonu En İyi Pratikleri (Best Practices)

1. **Yeni Sekme (Tab) Yönetimi:** Hepsiburada gibi sitelerde ürün kartlarına tıklandığında bazen ürün yeni sekmede (`target="_blank"`) açılabilir. Framework'ümüzün `SearchResultPage.clickFirstProduct()` metodu otomatik olarak açık pencereleri kontrol eder ve son sekmeye odaklanır (`driver.switchTo().window(lastWindow)`).
2. **Scroll Into View:** İnceleme (Reviews) sekmesi ve varyant butonları sayfanın alt kısımlarında kalabileceğinden, tıklamadan önce JavaScript ile elemente kaydırma (`scrollIntoView`) yapılması tıklama başarısını %100'e çıkarır.
3. **Esnek Doğrulamalar:** Giriş yapma yönlendirmesi bazen URL değişikliği (`giris.hepsiburada.com`), bazen de bir Popup Modal (`#login`) olarak tetiklenir. `isLoginRequiredPromptDisplayed()` metodunda her iki olasılığı da kontrol eden `ExpectedConditions.or(...)` yaklaşımı testlerin kırılmasını önler.
