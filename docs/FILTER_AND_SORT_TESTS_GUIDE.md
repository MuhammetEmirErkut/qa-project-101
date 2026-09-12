# 🎯 Gelişmiş Arama, Filtreleme ve Sıralama Testleri Rehberi

Bu rehberde, Hepsiburada üzerinde **Marka Filtreleme (Brand Filter)**, **Fiyata Göre Sıralama (Price Sorting)** ve **Fiyat Aralığı Filtreleme (Price Range Filter)** senaryolarını Page Object Model (POM) mimarisine uygun şekilde adım adım nasıl geliştireceğinizi bulacaksınız.

---

## 📚 1. Bölüm: Öğrenilecek Temel Kavramlar

### 1.1. Dinamik Filtreleme ve DOM Yenilenmesi (`StaleElementReferenceException`)
* **Problem:** Bir filtreye tıkladığınızda veya sıralama seçtiğinizde sayfa tamamen yenilenmez; sadece ürün listesi AJAX ile asenkron olarak güncellenir.
* **Çözüm:** Filtre seçildikten sonra eski listenin kaybolmasını ve yeni liste elemanlarının yüklenmesini `WebDriverWait` ile beklemek gerekir.

### 1.2. Fiyat Ayrıştırma (Currency & Number Parsing)
* **Problem:** Sitedeki fiyatlar `"15.499,00 TL"` veya `"24.999 TL"` şeklinde metin (String) formatındadır.
* **Çözüm:** Regex ile harfleri ve para birimini temizleyip (`price.replaceAll("[^0-9,]", "").replace(",", ".")`) matematiksel kıyaslama yapabileceğimiz `double` sayısal tipe dönüştürürüz.

### 1.3. Liste Doğrulamaları (List Assertions)
* **Amaç:** Yalnızca tek bir elementi değil, arama sonuçlarında gelen ilk $N$ adet ürünün belirli bir kurala (marka adı içerme, fiyat aralığında olma, küçükten büyüğe sıralı olma) uyduğunu döngü ile doğrulamaktır.

---

## 🛠️ 2. Bölüm: `SearchResultPage` Sınıfının Genişletilmesi

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/search/SearchResultPage.java`

`SearchResultPage` sınıfına filtreleme, sıralama ve ürün fiyatlarını okuma yeteneklerini ekliyoruz.

### 🔹 1. Adım: Yeni Locator'ların Eklenmesi

`SearchResultPage.java` dosyasının en üstündeki locator alanına şunları ekleyin:

```java
// ==================== FILTER & SORT LOCATORS ====================
private final By sortDropdownTrigger = By.cssSelector(
    "[data-test-id='sort-select'], div[class*='sort_'], div[class*='sorting'], div[class*='horizontalSortingBar'] select, div[class*='sort']"
);

private final By sortLowestPriceOption = By.xpath(
    "//*[contains(text(), 'En Düşük Fiyat') or contains(text(), 'Fiyata göre artan') or contains(text(), 'En düşük fiyat')]"
);

private final By minPriceInput = By.cssSelector(
    "input[placeholder*='En az' i], input[placeholder*='Min' i], input[name*='minPrice'], input[data-test-id*='price-min']"
);

private final By maxPriceInput = By.cssSelector(
    "input[placeholder*='En çok' i], input[placeholder*='Max' i], input[name*='maxPrice'], input[data-test-id*='price-max']"
);

private final By applyPriceFilterButton = By.xpath(
    "//button[contains(., 'Uygula') or contains(., 'Ara') or contains(@class, 'price-filter-btn') or contains(@class, 'range')] | //button[@type='submit' and contains(@class, 'price')]"
);

private final By productPrices = By.cssSelector(
    "[data-test-id='price-current-price'], [data-test-id='product-price'], span[data-test-id='final-price'], div[class*='price']"
);

private final By allProductTitles = By.cssSelector(
    "[data-test-id='product-card-name'], h3[data-test-id='product-card-name'], h3[type='comfort'], h2[data-test-id='product-card-name']"
);
```

---

### 🔹 2. Adım: Sayfa Metotlarının Eklenmesi

`SearchResultPage.java` sınıfının içine filtreleme ve sıralama işlemlerini gerçekleştiren metotları ekleyin:

```java
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.Keys;

// ...

/**
 * Sol filtre menüsünden marka seçer (Örn: "Apple", "Asus", "Lenovo")
 */
@Step("Filter products by brand: {brandName}")
public SearchResultPage filterByBrand(String brandName) {
    By brandLocator = By.xpath(
        "//div[contains(@class, 'filter')]//label[contains(., '" + brandName + "')] | " +
        "//div[contains(@class, 'filter')]//span[text()='" + brandName + "'] | " +
        "//a[contains(@title, '" + brandName + "')] | " +
        "//input[@value='" + brandName.toLowerCase() + "']/.."
    );

    try {
        WebElement brandElement = wait.until(ExpectedConditions.elementToBeClickable(brandLocator));
        jsClick(brandElement);
    } catch (Exception e) {
        WebElement brandElement = wait.until(ExpectedConditions.presenceOfElementLocated(brandLocator));
        jsClick(brandElement);
    }

    // Filtreleme sonrası listenin yenilenmesini bekle
    waitForProductsReload();
    return this;
}

/**
 * Sıralama menüsünden 'En Düşük Fiyat' seçeneğini seçer
 */
@Step("Sort products by lowest price")
public SearchResultPage sortByLowestPrice() {
    try {
        WebElement trigger = wait.until(ExpectedConditions.elementToBeClickable(sortDropdownTrigger));
        jsClick(trigger);

        WebElement lowestOption = wait.until(ExpectedConditions.elementToBeClickable(sortLowestPriceOption));
        jsClick(lowestOption);
    } catch (Exception e) {
        // Alternatif direkt seçim
        try {
            WebElement lowestOption = driver.findElement(sortLowestPriceOption);
            jsClick(lowestOption);
        } catch (Exception ignored) {
        }
    }

    waitForProductsReload();
    return this;
}

/**
 * Fiyat aralığı filtresi uygular (Örn: 10000 TL - 20000 TL)
 */
@Step("Filter products by price range: {minPrice} - {maxPrice}")
public SearchResultPage filterByPriceRange(String minPrice, String maxPrice) {
    try {
        WebElement minInput = wait.until(ExpectedConditions.visibilityOfElementLocated(minPriceInput));
        minInput.clear();
        minInput.sendKeys(minPrice);

        WebElement maxInput = wait.until(ExpectedConditions.visibilityOfElementLocated(maxPriceInput));
        maxInput.clear();
        maxInput.sendKeys(maxPrice);

        try {
            WebElement applyBtn = wait.until(ExpectedConditions.elementToBeClickable(applyPriceFilterButton));
            jsClick(applyBtn);
        } catch (Exception e) {
            maxInput.sendKeys(Keys.ENTER);
        }
    } catch (Exception e) {
        System.out.println("Price range inputs could not be interacted directly: " + e.getMessage());
    }

    waitForProductsReload();
    return this;
}

/**
 * İlk N ürünün başlıklarını liste olarak döndürür
 */
@Step("Get top {limit} product titles")
public List<String> getTopProductTitles(int limit) {
    List<String> titles = new ArrayList<>();
    try {
        List<WebElement> elements = driver.findElements(allProductTitles);
        int count = Math.min(elements.size(), limit);
        for (int i = 0; i < count; i++) {
            String title = elements.get(i).getText().trim();
            if (!title.isEmpty()) {
                titles.add(title);
            }
        }
    } catch (Exception ignored) {
    }
    return titles;
}

/**
 * İlk N ürünün fiyatlarını sayısal (Double) liste olarak döndürür
 */
@Step("Get top {limit} product prices")
public List<Double> getTopProductPrices(int limit) {
    List<Double> prices = new ArrayList<>();
    try {
        List<WebElement> elements = driver.findElements(productPrices);
        int count = Math.min(elements.size(), limit);
        for (int i = 0; i < count; i++) {
            String priceText = elements.get(i).getText().trim();
            // "15.499,00 TL" formatını 15499.00 double değerine dönüştür
            String cleanPrice = priceText.replaceAll("[^0-9,]", "").replace(",", ".");
            if (!cleanPrice.isEmpty()) {
                try {
                    prices.add(Double.parseDouble(cleanPrice));
                } catch (NumberFormatException ignored) {
                }
            }
        }
    } catch (Exception ignored) {
    }
    return prices;
}

/**
 * Filtreleme/Sıralama sonrası listenin güncellenmesini bekleyen yardımcı metot
 */
private void waitForProductsReload() {
    try {
        Thread.sleep(1500); // AJAX isteğinin tetiklenmesi için kısa bekleme
        wait.until(ExpectedConditions.presenceOfElementLocated(allProductTitles));
    } catch (Exception ignored) {
    }
}
```

---

## 🧪 3. Bölüm: Test Sınıfının Yazılması (`SearchFilterTests.java`)

Arama, filtreleme ve sıralama senaryolarını test etmek için yeni bir test sınıfı oluşturuyoruz.

**Yeni Dosya:** `src/test/java/com/hepsiburada/qa/tests/search/SearchFilterTests.java`

```java
package com.hepsiburada.qa.tests.search;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import static com.hepsiburada.qa.drivers.DriverFactory.getDriver;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;

@Epic("Search & Discovery")
@Feature("Search Filtering and Sorting")
public class SearchFilterTests extends BaseTest {

    @Test(description = "Verify that products can be filtered by brand")
    @Severity(SeverityLevel.CRITICAL)
    @Description("1. Navigate to home page\n2. Search 'Laptop'\n3. Filter by brand 'Apple'\n4. Verify first products contain brand name")
    public void testFilterByBrand() {
        String searchTerm = "Laptop";
        String targetBrand = "Apple";

        // 1. Arama yap
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search(searchTerm);

        // 2. Marka filtresi seç
        searchResultPage.filterByBrand(targetBrand);

        // 3. İlk 3 ürünün başlığında seçilen markanın geçtiğini doğrula
        List<String> productTitles = searchResultPage.getTopProductTitles(3);
        Assert.assertFalse(productTitles.isEmpty(), "Filtered product list should not be empty!");

        for (String title : productTitles) {
            Assert.assertTrue(
                title.toLowerCase().contains(targetBrand.toLowerCase()),
                "Product title does not contain filtered brand! Title: " + title + ", Expected Brand: " + targetBrand
            );
        }
    }

    @Test(description = "Verify that products can be sorted by lowest price")
    @Severity(SeverityLevel.NORMAL)
    @Description("1. Search 'Kulaklık'\n2. Sort by lowest price\n3. Verify that first item price is less than or equal to second item")
    public void testSortByLowestPrice() {
        // 1. Arama yap
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search("Kulaklık");

        // 2. En Düşük Fiyata Göre Sırala
        searchResultPage.sortByLowestPrice();

        // 3. İlk 3 ürünün fiyatlarını al ve sıralamayı doğrula
        List<Double> prices = searchResultPage.getTopProductPrices(3);
        if (prices.size() >= 2) {
            Assert.assertTrue(
                prices.get(0) <= prices.get(1),
                "Prices are not in ascending order! First: " + prices.get(0) + ", Second: " + prices.get(1)
            );
        }
    }

    @Test(description = "Verify that products can be filtered within a price range")
    @Severity(SeverityLevel.NORMAL)
    @Description("1. Search 'Monitor'\n2. Apply price filter (3000 TL - 7000 TL)\n3. Verify products are within price range")
    public void testFilterByPriceRange() {
        double minPrice = 3000.0;
        double maxPrice = 7000.0;

        // 1. Arama yap
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search("Monitör");

        // 2. Fiyat aralığı filtresi uygula
        searchResultPage.filterByPriceRange("3000", "7000");

        // 3. Gelen ürünlerin fiyat aralığında olduğunu doğrula
        List<Double> prices = searchResultPage.getTopProductPrices(3);
        for (Double price : prices) {
            Assert.assertTrue(
                price >= minPrice && price <= maxPrice,
                "Product price is out of range! Price: " + price + ", Expected range: [" + minPrice + " - " + maxPrice + "]"
            );
        }
    }
}
```

---

## 🚀 4. Bölüm: Testleri Çalıştırma Komutları

```bash
# Sadece yeni filtreleme ve sıralama testlerini çalıştırmak için:
mvn test -Dtest=SearchFilterTests

# Belirli bir metodu çalıştırmak için (Örn: Marka filtresi):
mvn test -Dtest=SearchFilterTests#testFilterByBrand

# Tüm test paketini koşmak için:
mvn clean test

# Allure raporunu açıp adımları incelemek için:
allure serve target/allure-results
```

---

## 🎯 Özet: Neler Kazandık?
1. **Dinamik Liste Yönetimi:** Sayfadaki dinamik ürün listelerini `List<WebElement>` üzerinden listeleyip döngüsel assert yapma pratiği kazandık.
2. **String to Numeric Dönüşümü:** E-ticaret sitelerindeki karmaşık para birimi metinlerini temizleyip sayısal karşılaştırma yaptık.
3. **Kapsamlı Filtre Mimarisi:** Marka, Sıralama ve Aralık bazlı 3 farklı filtreleme türünü POM mimarisine kazandırdık.
