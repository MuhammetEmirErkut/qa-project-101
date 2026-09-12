# 🧪 Hızlı Eklenebilir Testler Rehberi (Negatif Arama & Sepetten Ürün Silme)

Bu rehberde, mevcut Page Object Model mimarisini genişleterek projenize **Negatif Arama (Sonuç Bulunamadı)** ve **Sepetten Ürün Silme (Sepeti Boşaltma)** testlerini adım adım nasıl ekleyeceğinizi bulacaksınız.

---

## 📚 1. Bölüm: Öğrenilecek Kavramlar ve Test Mantığı

### 1.1. Negatif Test (Negative Testing) Nedir?
* **Amaç:** Sistemin sadece doğru verilerle değil, beklenmeyen / geçersiz verilerle de çökmeksizin doğru hata mesajı veya bilgilendirme ekranı gösterdiğini doğrulamaktır.
* **Senaryomuz:** Arama kutusuna anlamsız bir kelime (`"zxcvbnm12345xyz"`) yazıldığında uygulamanın hata vermeden *"Sonuç bulunamadı"* mesajı döndürdüğünün ve ürün listesinin boş olduğunun doğrulanması.

### 1.2. State-Changing Cart Action (Sepet Durum Değişikliği)
* **Amaç:** Sepetteki dinamik DOM değişikliklerini ve asynchronous (AJAX) silme işlemlerini stabil şekilde doğrulamaktır.
* **Senaryomuz:** Sepete bir ürün eklenip sepete gidildikten sonra silme butonuna tıklanması, ürünün listeden kaybolmasının ve *"Sepetiniz boş"* durumunun teyit edilmesi.

---

## 🛠️ 2. Bölüm: Adım Adım Kod Değişiklikleri

---

### 🔹 1. Adım: SearchResultPage'e Negatif Arama Locator ve Metotlarını Ekleme

Arama sonucunda ürün bulunamadığında çıkan uyarı metnini veya boş durum container'ını yakalayacak locator ve metotları ekliyoruz.

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/search/SearchResultPage.java`

#### Eklenecek Locator'lar:
```java
private final By noResultsContainer = By.cssSelector(
    "div[class*='noResult'], div[class*='not-found'], [data-test-id='no-search-result'], div[class*='empty-search']"
);

private final By noResultsMessage = By.xpath(
    "//*[contains(text(), 'bulunamadı') or contains(text(), 'sonuç bulunamadı') or contains(text(), 'arama kriterlerine uygun')]"
);
```

#### Eklenecek Metotlar:
```java
@Step("Check if no results message is displayed")
public boolean isNoResultsMessageDisplayed() {
    try {
        return isDisplayed(noResultsMessage) || isDisplayed(noResultsContainer);
    } catch (Exception e) {
        return false;
    }
}

@Step("Get no results message text")
public String getNoResultsMessageText() {
    try {
        if (isDisplayed(noResultsMessage)) {
            return getText(noResultsMessage);
        }
    } catch (Exception ignored) {
    }
    return "";
}
```

---

### 🔹 2. Adım: SearchTests Sınıfına Negatif Arama Testini Ekleme

`SearchTests.java` dosyasına negatif arama senaryosunu içeren yeni bir `@Test` metodu ekliyoruz.

**Dosya:** `src/test/java/com/hepsiburada/qa/tests/search/SearchTests.java`

```java
@Test(description = "Verify that appropriate message is displayed when searching for non-existing keyword")
@Severity(SeverityLevel.NORMAL)
@Description("1. Navigate to home page\n2. Search for a non-existing product\n3. Verify zero results or no results message")
public void testSearchNonExistingProduct() {
    String invalidKeyword = "zxcvbnm987654321xyz";

    HomePage homePage = new HomePage(getDriver());
    homePage.handleCookies();
    SearchResultPage searchResultPage = homePage.search(invalidKeyword);

    int resultCount = searchResultPage.getResultCount();
    boolean hasNoResultMessage = searchResultPage.isNoResultsMessageDisplayed();

    Assert.assertTrue(
        resultCount == 0 || hasNoResultMessage,
        "Search with invalid keyword should return 0 results or display no-results message! Result count: " + resultCount
    );
}
```

---

### 🔹 3. Adım: CartPage'e Ürün Silme Locator ve Metotlarını Ekleme

Sepetteki ürünü kaldırmak için silme butonuna tıklayan ve sepetin boşaldığını kontrol eden metotları ekliyoruz.

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/cart/CartPage.java`

#### Eklenecek Locator'lar:
```java
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
```

#### Eklenecek Metotlar:
```java
@Step("Remove first product from cart")
public CartPage removeFirstProduct() {
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
        } catch (Exception ignored) {
        }
    }

    // Varsa onay modalındaki 'Sil' butonuna tıkla
    try {
        WebDriverWait modalWait = new WebDriverWait(driver, Duration.ofSeconds(4));
        WebElement confirmBtn = modalWait.until(ExpectedConditions.elementToBeClickable(confirmDeleteModalButton));
        jsClick(confirmBtn);
    } catch (Exception ignored) {
    }

    // Sepetin güncellenmesini bekle
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

@Step("Check if cart is empty")
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
```

---

### 🔹 4. Adım: CartTests Sınıfına "Sepetten Ürün Silme" Testini Ekleme

`CartTests.java` sınıfına ürün ekleyip ardından silen ve sepetin boş olduğunu doğrulayan uçtan uca testi ekliyoruz.

**Dosya:** `src/test/java/com/hepsiburada/qa/tests/cart/CartTests.java`

```java
@Test(description = "Users should be able to remove an item from the cart")
@Severity(SeverityLevel.CRITICAL)
@Description("1. Navigate to home page\n2. Search product\n3. Add product to cart\n4. Navigate to cart\n5. Remove item from cart\n6. Verify cart is empty")
public void testRemoveProductFromCart() {
    // 1. Search and Navigate to PDP
    HomePage homePage = new HomePage(getDriver());
    homePage.handleCookies();
    SearchResultPage searchResultPage = homePage.searchForProduct("Bluetooth Kulaklık");

    ProductDetailPage pdp = searchResultPage.clickFirstProduct();

    // 2. Add to Cart & Go to Cart
    CartPage cartPage = pdp.addToCart().goToCart();
    Assert.assertTrue(cartPage.getCartItemCount() > 0, "Cart should contain at least 1 item before removal!");

    // 3. Remove Item
    cartPage.removeFirstProduct();

    // 4. Verify Cart is Empty
    Assert.assertTrue(cartPage.isCartEmpty(), "Cart should be empty after deleting the product!");
}
```

---

## 🚀 3. Bölüm: Testleri Çalıştırma ve Doğrulama

Terminalden testleri koşturmak için:

```bash
# Tüm testleri çalıştırma
mvn clean test

# Sadece arama testlerini çalıştırma
mvn test -Dtest=SearchTests

# Sadece sepet testlerini çalıştırma
mvn test -Dtest=CartTests

# Allure raporunu canlı inceleme
allure serve target/allure-results
```

---

## 🎯 Özet: Neler Kazandık?
1. **Negatif Senaryo Kapsamı:** Arama motorunun başarısız aramalarda doğru davrandığını test ettik.
2. **Sepet Döngüsü Tamamlandı:** Sepete sadece ürün eklemekle kalmayıp, silme (CRUD operasyonunun Delete adımı) işlemini de otomasyona dahil ettik.
3. **Temiz POM Mimarisi:** Kod tekrarından kaçınarak tüm locator ve aksiyonları ilgili sayfa nesnelerine kapsülledik (Encapsulation).
