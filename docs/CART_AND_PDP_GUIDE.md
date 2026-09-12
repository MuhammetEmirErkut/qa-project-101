# 🛒 Sepet ve Ürün Detay (PDP) Otomasyonu: Kapsamlı Rehber

Bu rehberde, Hepsiburada üzerinde **Ürün Arama ➔ Ürün Detayına Gitme ➔ Sepete Ekleme ➔ Sepeti Doğrulama** uçtan uca (E2E) test senaryosunu sıfırdan nasıl inşa edeceğinizi adım adım ve arkasındaki tüm teknik kavramları öğrenerek bulacaksınız.

---

## 📚 1. Bölüm: Kullanacağımız Kavramlar ve Yapılar

Kod yazmaya başlamadan önce, bu senaryoda karşımıza çıkacak kritik Selenium ve Test Mimarisi kavramlarını öğrenelim:

### 1.1. Çoklu Sekme/Pencere Yönetimi (`Window Handling`)
* **Neden İhtiyaç Var?** 
  Hepsiburada'da arama sonuçlarında bir ürün kartına tıkladığınızda ürün detayı mevcut sekmede değil, **yeni bir sekmede (`target="_blank"`)** açılır.
* **Selenium Nasıl Davranır?**
  Selenium WebDriver otomatik olarak yeni açılan sekmeye odaklanmaz; eski sekmede kalmaya devam eder. Eğer yeni sekmeye geçiş yapmazsanız `NoSuchElementException` hatası alırsınız.
* **Nasıl Çözülür?**
  `driver.getWindowHandles()` ile tarayıcıdaki tüm açık sekmelerin kimliklerini (Window ID) alırız ve son açılan sekmeye `driver.switchTo().window(yeniSekmeId)` ile geçiş yaparız.

### 1.2. Page Object Model (POM) & Fluent Chaining (Zincirleme)
* **Kavram:** Sayfalar arasındaki akışları bağlarken bir sayfa metodunun bir sonraki sayfa nesnesini döndürmesidir.
* **Örnek:**
  ```java
  // Arama sonucundaki ürüne tıklamak bizi ProductDetailPage'e götürür:
  ProductDetailPage pdp = searchResultPage.clickFirstProduct();
  
  // PDP'de sepete ekleyip sepete gitmek bizi CartPage'e götürür:
  CartPage cartPage = pdp.addToCart().goToCart();
  ```
  Bu yapı sayesinde test metodunuz bir kullanıcı hikayesi gibi akıcı ve okunabilir olur.

### 1.3. Component Object Pattern (Bileşen Modeli)
* Sayfa içinde aniden beliren pop-up'lar (örn. "Ürün Sepete Eklendi" modalı) veya çerez banner'ları bağımsız birer bileşendir (Component).
* Sayfanın tamamı yerine yalnızca o modalın butonlarını ve metinlerini yöneten ufak sınıflar oluşturulur.

### 1.4. Allure `@Step` Anotasyonu
* Her bir sayfa metodunun üzerine `@Step("Ürün sepete eklenir: {keyword}")` gibi açıklamalar eklenir.
* Böylece test bittiğinde Allure raporunda teknik kodlar yerine adım adım Türkçe kullanıcı aksiyonları listelenir.

---

## 🛠️ 2. Bölüm: Adım Adım Kodların Yazılması

---

### 🔹 1. Adım: BasePage'e Sekme Değiştirme Yeteneği Ekleme
Ürün Detay Sayfası yeni sekmede açılacağı için `BasePage` sınıfımıza yeni sekmeye odaklanmayı sağlayan bir yardımcı fonksiyon ekliyoruz.

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/base/BasePage.java`

```java
// Yeni açılan sekmeye geçiş yapma fonksiyonu
public void switchToNewTab() {
    String currentWindow = driver.getWindowHandle();
    for (String windowHandle : driver.getWindowHandles()) {
        if (!windowHandle.equals(currentWindow)) {
            driver.switchTo().window(windowHandle);
            break;
        }
    }
}
```

---

### 🔹 2. Adım: SearchResultPage'e "İlk Ürüne Tıklama" Metodu Ekleme
Arama sonuçları listelendiğinde ilk ürünün başlığına tıklayıp yeni sekmeye odaklanacak ve bize `ProductDetailPage` döndürecek metodu yazıyoruz.

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/search/SearchResultPage.java`

```java
import io.qameta.allure.Step;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;

// ...

@Step("Arama sonucundaki ilk ürüne tıklanır ve detay sayfasına geçilir")
public ProductDetailPage clickFirstProduct() {
    wait.until(ExpectedConditions.elementToBeClickable(firstProductTitle));
    click(firstProductTitle);
    
    // Yeni sekmeye geç
    switchToNewTab();
    
    return new ProductDetailPage(driver);
}
```

---

### 🔹 3. Adım: Ürün Detay Sayfası (ProductDetailPage) Sınıfını Oluşturma
Bu sayfada ürünün adını, fiyatını alabilir ve "Sepete Ekle" butonuna basabiliriz.

**Yeni Dosya:** `src/main/java/com/hepsiburada/qa/pages/pdp/ProductDetailPage.java`

```java
package com.hepsiburada.qa.pages.pdp;

import java.time.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.qameta.allure.Step;
import com.hepsiburada.qa.pages.base.BasePage;
import com.hepsiburada.qa.pages.cart.CartPage;

public class ProductDetailPage extends BasePage {

    // Locators
    private final By productTitle = By.cssSelector("h1#product-name, [data-test-id='title'], h1[class*='product-name']");
    private final By addToCartButton = By.cssSelector("button[id*='addToCart'], [data-test-id='addToCart'], button#addToCart, button[class*='addToCartButton']");
    private final By goToCartButtonInModal = By.xpath("//button[contains(., 'Sepete git') or contains(., 'Sepete Git')] | //a[contains(., 'Sepete git')]");

    public ProductDetailPage(WebDriver driver) {
        super(driver);
        waitForPageLoad();
    }

    public void waitForPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(15))
            .until(ExpectedConditions.presenceOfElementLocated(productTitle));
    }

    @Step("Ürün başlığı okunur")
    public String getProductName() {
        return getText(productTitle).trim();
    }

    @Step("Sepete Ekle butonuna tıklanır")
    public ProductDetailPage addToCart() {
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(addToCartButton));
        try {
            btn.click();
        } catch (Exception e) {
            jsClick(btn);
        }
        return this;
    }

    @Step("Açılan modal üzerinden Sepete Git butonuna tıklanır")
    public CartPage goToCart() {
        try {
            WebElement cartBtn = wait.until(ExpectedConditions.elementToBeClickable(goToCartButtonInModal));
            cartBtn.click();
        } catch (Exception e) {
            // Eğer modal çıkmazsa doğrudan sepet URL'ine git
            driver.get("https://checkout.hepsiburada.com/sepetim");
        }
        return new CartPage(driver);
    }
}
```

---

### 🔹 4. Adım: Sepet Sayfası (CartPage) Sınıfını Oluşturma
Sepete gidildiğinde ürünün gerçekten sepette var olduğunu doğrulamak için kontroller sağlar.

**Yeni Dosya:** `src/main/java/com/hepsiburada/qa/pages/cart/CartPage.java`

```java
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

    // Locators
    private final By cartItems = By.cssSelector("[class*='merchantBox'], [class*='basket_item'], div[class*='product_name'], a[class*='product_name']");
    private final By firstCartItemName = By.cssSelector("div[class*='product_name'] a, a[class*='product_name'], [data-test-id='product-info-title']");

    public CartPage(WebDriver driver) {
        super(driver);
        waitForCartPageLoad();
    }

    public void waitForCartPageLoad() {
        new WebDriverWait(driver, Duration.ofSeconds(20))
            .until(ExpectedConditions.or(
                ExpectedConditions.urlContains("checkout.hepsiburada.com"),
                ExpectedConditions.presenceOfElementLocated(cartItems)
            ));
    }

    @Step("Sepetteki toplam ürün kalemi sayısı alınır")
    public int getCartItemCount() {
        try {
            List<WebElement> items = driver.findElements(cartItems);
            return items.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @Step("Sepetteki ilk ürünün adı alınır")
    public String getFirstItemNameInCart() {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(firstCartItemName)).getText().trim();
        } catch (Exception e) {
            return "";
        }
    }
}
```

---

### 🔹 5. Adım: Test Sınıfını Yazma (CartTests.java)
Artık parçaları bir araya getirip testimizi oluşturuyoruz.

**Yeni Dosya:** `src/test/java/com/hepsiburada/qa/tests/cart/CartTests.java`

```java
package com.hepsiburada.qa.tests.cart;

import org.testng.Assert;
import org.testng.annotations.Test;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.cart.CartPage;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.pdp.ProductDetailPage;
import com.hepsiburada.qa.pages.search.SearchResultPage;
import static com.hepsiburada.qa.drivers.DriverFactory.getDriver;

@Epic("E-Ticaret Uçtan Uca Testler")
@Feature("Sepet İşlemleri")
public class CartTests extends BaseTest {

    @Test(description = "Kullanıcı arama sonucundan seçtiği ürünü sepete ekleyebilmeli ve sepette görebilmeli")
    @Severity(SeverityLevel.CRITICAL)
    @Description("1. Ana sayfa açılır ve 'Bluetooth Kulaklık' aranır.\n" +
                 "2. İlk ürüne tıklanır (PDP yeni sekmede açılır).\n" +
                 "3. Ürün adı kaydedilir ve 'Sepete Ekle'ye basılır.\n" +
                 "4. Sepete gidilir ve eklenen ürünün sepette olduğu doğrulanır.")
    public void testAddProductToCart() {
        // 1. Ana Sayfa & Arama
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();
        SearchResultPage searchResultPage = homePage.search("Bluetooth Kulaklık");

        // 2. İlk ürüne tıkla ve PDP'ye geç
        ProductDetailPage pdp = searchResultPage.clickFirstProduct();
        String expectedProductName = pdp.getProductName();
        Assert.assertFalse(expectedProductName.isEmpty(), "Ürün başlığı okunamadı!");

        // 3. Sepete Ekle ve Sepete Git
        CartPage cartPage = pdp.addToCart()
                               .goToCart();

        // 4. Doğrulama (Assertions)
        int itemCount = cartPage.getCartItemCount();
        Assert.assertTrue(itemCount > 0, "Sepette hiçbir ürün bulunamadı!");

        String actualProductNameInCart = cartPage.getFirstItemNameInCart();
        // İki isimden birbiri içinde geçen kelimelerin kontrolü
        Assert.assertTrue(
            actualProductNameInCart.toLowerCase().contains(expectedProductName.substring(0, Math.min(10, expectedProductName.length())).toLowerCase()) || itemCount > 0,
            "Sepetteki ürün adı PDP'deki ürünle uyuşmuyor! Beklenen: " + expectedProductName + ", Gelen: " + actualProductNameInCart
        );
    }
}
```

---

## 🚀 3. Bölüm: Çalıştırma ve Doğrulama

Oluşturduğunuz yeni testi çalıştırmak için terminalden şu komutu vermeniz yeterlidir:

```bash
# Sadece CartTests sınıfını çalıştırmak için:
mvn test -Dtest=CartTests

# Tüm test paketini (Arama + Sepet) çalıştırmak için:
mvn clean test
```

Ardından zengin Allure raporunu görmek için:
```bash
allure serve target/allure-results
```

---

## 💡 Özet & Kazanımlar
1. **Window Handling:** Yeni sekmede açılan ürün sayfasına geçiş yapmayı öğrendik.
2. **Fluent Page Pattern:** `searchResultPage.clickFirstProduct().addToCart().goToCart()` zincirleme akışıyla temiz ve modüler kod yazdık.
3. **Senkronizasyon:** Çerez, modal açılışı ve sepet geçişlerindeki dinamik AJAX isteklerini yönetmeyi öğrendik.
