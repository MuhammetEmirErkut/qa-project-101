# 🚀 Kurumsal Test Otomasyonu Mimari ve Geliştirme Rehberi
> **Hedef:** Sahibinden.com Web Test Otomasyon Projesi  
> **Yaklaşım:** Page Object Model (POM), Bileşen Tabanlı (Component-Based) & Veri Güdümlü (Data-Driven) Mimari  
> **Amaç:** Her yapının amacını, çalışma mantığını ve parça parça kod örneklerini anlayarak sıfırdan kurumsal bir test otomasyon projesi inşa etmek.

---

## 📑 İçindekiler
1. [Büyük Resim: Test Otomasyonu Mimarisi](#1-büyük-resim-test-otomasyonu-mimarisi)
2. [Klasör Hiyerarşisi ve Katmanlar](#2-klasör-hiyerarşisi-ve-katmanlar)
3. [Her Yapının Detaylı Açıklaması ve Parça Kodları](#3-her-yapının-detaylı-açıklaması-ve-parça-kodları)
   - [3.1. ConfigReader & config.properties (Ayar Yönetimi)](#31-configreader--configproperties-ayar-yönetimi)
   - [3.2. DriverFactory & ThreadLocal (Sürücü Yönetimi)](#32-driverfactory--threadlocal-sürücü-yönetimi)
   - [3.3. BasePage & Dinamik Beklemeler (Ortak Sayfa Katmanı)](#33-basepage--dinamik-beklemeler-ortak-sayfa-katmanı)
   - [3.4. Page Objects ve Bileşenler (Components)](#34-page-objects-ve-bileşenler-components)
   - [3.5. BaseTest (Test Yaşam Döngüsü)](#35-basetest-test-yaşam-döngüsü)
   - [3.6. Test Sınıfları ve Doğrulama (Assertion) Stratejileri](#36-test-sınıfları-ve-doğrulama-assertion-stratejileri)
   - [3.7. TestListener & Ekran Görüntüsü Alma (Hata Yönetimi)](#37-testlistener--ekran-görüntüsü-alma-hata-yönetimi)
   - [3.8. Data-Driven Testing (@DataProvider ile Veri Güdümlü Test)](#38-data-driven-testing-dataprovider-ile-veri-güdümlü-test)
   - [3.9. testng.xml ve Paralel Koşum Yapılandırması](#39-testngxml-ve-paralel-koşum-yapılandırması)
4. [Sahibinden.com Özelinde Zorluklar ve Çözümleri](#4-sahibindencom-özelinde-zorluklar-ve-çözümleri)
5. [Altın Kurallar & Yapılmaması Gereken Hatalar](#5-altın-kurallar--yapılmaması-gereken-hatalar)

---

## 1. Büyük Resim: Test Otomasyonu Mimarisi

Bir test otomasyon projesinde her katmanın tek bir sorumluluğu (**Single Responsibility Principle**) olmalıdır:

```
┌─────────────────────────────────────────────────────────────┐
│                        TEST LAYER                           │
│  (VehicleSearchTests, HomePageTests)                        │
│  - "Ne test edilecek?" sorusuna cevap verir.                │
│  - Sayfa elementlerini BİLMEZ.                              │
│  - Sadece iş akışını çağırır ve Assert (Doğrulama) yapar.   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Metot çağrıları (örn: searchItem("BMW"))
┌──────────────────────────────▼──────────────────────────────┐
│                     PAGE OBJECT LAYER                       │
│  (HomePage, SearchResultsPage, CookieBannerComponent)       │
│  - "Nasıl etkileşim kurulur?" sorusuna cevap verir.         │
│  - Butonların, inputların 'By' locator'larını tutar.         │
│  - Asla ASSERT yapmaz; sadece aksiyon alıp veri döner.      │
└──────────────────────────────┬──────────────────────────────┘
                               │ Element bulma ve bekleme çağrıları
┌──────────────────────────────▼──────────────────────────────┐
│                    CORE / UTILITY LAYER                     │
│  (DriverFactory, ConfigReader, BasePage, WaitUtils)         │
│  - Tarayıcıyı ayağa kaldırır, ayarları okur, beklemeleri    │
│    güvenli şekilde yönetir.                                 │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Klasör Hiyerarşisi ve Katmanlar

```text
src/
├── main/
│   ├── java/com/sahibinden/qa/
│   │   ├── config/                  # Ayar dosyası okuyucuları
│   │   ├── drivers/                 # Tarayıcı motoru ve ThreadLocal Driver
│   │   ├── pages/                   # Web Sayfaları
│   │   │   ├── base/                # BasePage (Tüm sayfalara miras kalan ortak sınıf)
│   │   │   ├── home/                # Ana sayfa ve alt bileşenleri (Header, Cookie)
│   │   │   ├── search/              # Arama sonuç sayfası ve filtre bileşeni
│   │   │   └── detail/              # İlan detay sayfası
│   │   └── utils/                   # Screenshot, Javascript, Wait yardımcıları
│   └── resources/
│
└── test/
    ├── java/com/sahibinden/qa/
    │   ├── base/                    # BaseTest (@BeforeMethod, @AfterMethod)
    │   ├── listeners/               # Test dinleyicileri (Hata anında rapor/screenshot)
    │   ├── dataprovider/            # Çoklu test verisi sağlayıcıları
    │   └── tests/                   # Asıl Test Senaryoları
    └── resources/
        ├── config/
        │   └── config.properties    # URL, timeout ve tarayıcı ayarları
        └── suites/
            ├── smoke-tests.xml      # Hızlı kritik kontroller
            └── regression-tests.xml # Kapsamlı tüm testler
```

---

## 3. Her Yapının Detaylı Açıklaması ve Parça Kodları

---

### 3.1. ConfigReader & config.properties (Ayar Yönetimi)

#### 🎯 Ne İşe Yarar?
Test yazarken URL (`https://www.sahibinden.com`), tarayıcı adı (`chrome`), bekleme süresi (`15`) gibi bilgileri Java kodunun içine doğrudan yazmak (**hardcoding**) kötü bir pratiktir. Bu değerleri tek bir metin dosyasında tutarız.

#### ⚙️ Nasıl Çalışır?
1. **`config.properties` Dosyası:** `src/test/resources/config/` altına konur. Anahtar-Değer (`key=value`) şeklinde saklanır:
   ```properties
   browser=chrome
   baseUrl=https://www.sahibinden.com
   explicitWait=15
   headless=false
   ```
2. **`ConfigReader.java` Arayüzü:** `Owner` kütüphanesi sayesinde dosya okuma işlemi tek bir interface ile çözülür:
   ```java
   package com.sahibinden.qa.config;

   import org.aeonbits.owner.Config;
   import org.aeonbits.owner.Config.Sources;

   @Sources("classpath:config/config.properties")
   public interface ConfigReader extends Config {
       
       @Key("browser")
       @DefaultValue("chrome")
       String browser();

       @Key("baseUrl")
       String baseUrl();

       @Key("explicitWait")
       @DefaultValue("15")
       int explicitWait();

       @Key("headless")
       @DefaultValue("false")
       boolean headless();
   }
   ```
3. **Kullanımı:**
   ```java
   ConfigReader config = ConfigFactory.create(ConfigReader.class);
   String url = config.baseUrl(); // "https://www.sahibinden.com" döner
   ```

---

### 3.2. DriverFactory & ThreadLocal (Sürücü Yönetimi)

#### 🎯 Ne İşe Yarar?
Test başlamadan önce Chrome/Firefox tarayıcısını ayağa kaldıran, pencere boyutunu ayarlayan, bot tespitini önleyen ayarları yükleyen ve test bittiğinde tarayıcıyı temiz bir şekilde kapatan sınıftır.

#### ⚙️ Neden `ThreadLocal<WebDriver>`?
* Eğer sadece `public static WebDriver driver;` kullanırsanız, aynı anda birden fazla test çalıştığında tüm testler aynı tarayıcıyı kontrol etmeye çalışır ve testler birbirini ezer.
* `ThreadLocal<WebDriver>` her teste (her thread'e) **kendisine ait özel bir WebDriver** nesnesi verir.

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.drivers;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import java.time.Duration;

public class DriverFactory {
    
    // Her thread için izole edilmiş driver havuzu:
    private static final ThreadLocal<WebDriver> driverPool = new ThreadLocal<>();

    // Constructor'ı private yaparak dışarıdan "new DriverFactory()" denmesini engelleriz
    private DriverFactory() {}

    public static void initDriver(String browser) {
        if (driverPool.get() == null) {
            switch (browser.toLowerCase()) {
                case "chrome":
                    ChromeOptions options = new ChromeOptions();
                    options.addArguments("--disable-blink-features=AutomationControlled");
                    options.addArguments("--start-maximized");
                    // Selenium 4 yerleşik olarak chromedriver binary'sini otomatik yönetir:
                    driverPool.set(new ChromeDriver(options));
                    break;
                // İhtiyaca göre firefox, edge eklenebilir
            }
        }
    }

    public static WebDriver getDriver() {
        return driverPool.get();
    }

    public static void quitDriver() {
        if (driverPool.get() != null) {
            driverPool.get().quit();
            driverPool.remove(); // Hafızayı temizle (Memory leak önleme)
        }
    }
}
```

---

### 3.3. BasePage & Dinamik Beklemeler (Ortak Sayfa Katmanı)

#### 🎯 Ne İşe Yarar?
Tüm Page sınıflarının (HomePage, SearchResultsPage vb.) miras alacağı (`extends`) ata sınıftır. Sayfada bir butona tıklamadan veya bir kutuya yazı yazmadan önce **o elemanın hazır olmasını bekleyen güvenli metotlar** barındırır.

#### ⚙️ Neden `Thread.sleep()` Değil de `WebDriverWait`?
* `Thread.sleep(5000)`: Sayfa 0.5 saniyede yüklense bile 5 saniye bekletir, testler gereksiz uzar.
* `WebDriverWait` (Explicit Wait): "Maksimum 15 saniye bekle ama eleman 200. milisaniyede hazır olursa HEMEN tıkla ve devam et" der.

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.pages.base;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

public abstract class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;

    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    // Güvenli Tıklama: Eleman tıklanabilir olana kadar bekle, sonra tıkla
    protected void click(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    // Güvenli Metin Yazma: Eleman görünür olana kadar bekle, temizle ve yaz
    protected void sendKeys(By locator, String text) {
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    // Güvenli Metin Okuma
    protected String getText(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).getText().trim();
    }

    // Elemanın Görünürlüğünü Kontrol Etme (Hata fırlatmadan boolean döner)
    protected boolean isDisplayed(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

### 3.4. Page Objects ve Bileşenler (Components)

#### 🎯 Ne İşe Yarar?
* **Page Object:** Sahibinden.com'daki bir web sayfasını temsil eder (örn: `HomePage`, `SearchResultsPage`).
* **Component:** Birçok sayfada ortak olan parçaları temsil eder (örn: `CookieBannerComponent`, `HeaderComponent`).

#### ⚙️ Kurallar:
1. Locator'lar (`By`) `private` olmalıdır (Dışarıdan doğrudan erişilemez).
2. Page metotları iş akışını yapar (örn: `searchProduct(String keyword)`).
3. **Asla Page içinde `Assert` yapılmaz.** Page sınıfı durum bilgisi döner, assertion'ı test sınıfı yapar.

#### 🧩 Parça Kod Mantığı (1. Çerez Bileşeni):
```java
package com.sahibinden.qa.pages.home.components;

import com.sahibinden.qa.pages.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class CookieBannerComponent extends BasePage {
    
    // Çerez kabul butonu locator'ı
    private final By acceptCookiesButton = By.id("onetrust-accept-btn-handler");

    public CookieBannerComponent(WebDriver driver) {
        super(driver);
    }

    public void acceptCookiesIfPresent() {
        if (isDisplayed(acceptCookiesButton)) {
            click(acceptCookiesButton);
        }
    }
}
```

#### 🧩 Parça Kod Mantığı (2. HomePage):
```java
package com.sahibinden.qa.pages.home;

import com.sahibinden.qa.pages.base.BasePage;
import com.sahibinden.qa.pages.home.components.CookieBannerComponent;
import com.sahibinden.qa.pages.search.SearchResultsPage;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;

public class HomePage extends BasePage {
    
    // Locator'lar
    private final By searchInput = By.id("searchText");
    
    // Bileşenler
    private final CookieBannerComponent cookieBanner;

    public HomePage(WebDriver driver) {
        super(driver);
        this.cookieBanner = new CookieBannerComponent(driver);
    }

    public void handleCookies() {
        cookieBanner.acceptCookiesIfPresent();
    }

    // Arama yapıp yeni açılan sayfayı (SearchResultsPage) nesne olarak dönen metot (Fluent Pattern):
    public SearchResultsPage search(String keyword) {
        sendKeys(searchInput, keyword);
        driver.findElement(searchInput).sendKeys(Keys.ENTER);
        return new SearchResultsPage(driver);
    }
}
```

---

### 3.5. BaseTest (Test Yaşam Döngüsü)

#### 🎯 Ne İşe Yarar?
Her testten önce (`@BeforeMethod`) tarayıcıyı başlatan ve URL'e giden, her test bittikten sonra (`@AfterMethod`) tarayıcıyı kapatan sınıftır. Böylece test metotlarınızın içinde `driver.get()` veya `driver.quit()` yazmak zorunda kalmazsınız.

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.base;

import com.sahibinden.qa.config.ConfigReader;
import com.sahibinden.qa.drivers.DriverFactory;
import org.aeonbits.owner.ConfigFactory;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BaseTest {
    protected ConfigReader config;

    @BeforeMethod
    public void setUp() {
        // Konfigürasyonu oku
        config = ConfigFactory.create(ConfigReader.class);
        
        // Driver'ı başlat ve siteye git
        DriverFactory.initDriver(config.browser());
        getDriver().get(config.baseUrl());
    }

    protected WebDriver getDriver() {
        return DriverFactory.getDriver();
    }

    @AfterMethod
    public void tearDown() {
        // Tarayıcıyı kapat
        DriverFactory.quitDriver();
    }
}
```

---

### 3.6. Test Sınıfları ve Doğrulama (Assertion) Stratejileri

#### 🎯 Ne İşe Yarar?
Kullanıcı senaryosunu baştan sona çalıştırır ve beklenen sonuç ile gerçekleşen sonucu karşılaştırır (`Assert`).

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.tests.search;

import com.sahibinden.qa.base.BaseTest;
import com.sahibinden.qa.pages.home.HomePage;
import com.sahibinden.qa.pages.search.SearchResultsPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SearchTests extends BaseTest {

    @Test(description = "Kullanıcı arama yaptığında ilgili sonuçların listelendiği doğrulanmalıdır")
    public void testSearchVehicles() {
        HomePage homePage = new HomePage(getDriver());
        
        // 1. Çerezleri kabul et
        homePage.handleCookies();

        // 2. Arama yap ve sonuç sayfasına geç
        SearchResultsPage resultsPage = homePage.search("BMW 320i");

        // 3. DOĞRULAMA (Assertion): Sonuç listesi dolu mu ve başlık aranan kelimeyi içeriyor mu?
        int resultCount = resultsPage.getResultCount();
        Assert.assertTrue(resultCount > 0, "Arama sonucu 0'dan büyük olmalıdır!");
        
        String firstTitle = resultsPage.getFirstResultTitle();
        Assert.assertTrue(firstTitle.toLowerCase().contains("bmw"), "İlk ilan başlığı BMW içermelidir!");
    }
}
```

---

### 3.7. TestListener & Ekran Görüntüsü Alma (Hata Yönetimi)

#### 🎯 Ne İşe Yarar?
Testler koşarken TestNG olaylarını dinler. Eğer bir test **FAIL (Başarısız)** olursa, otomatik olarak o anın ekran görüntüsünü (screenshot) çeker ve rapora ekler. Böylece testin neden patladığını anında görürsünüz.

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.listeners;

import com.sahibinden.qa.drivers.DriverFactory;
import io.qameta.allure.Attachment;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestListener implements ITestListener {

    @Override
    public void onTestFailure(ITestResult result) {
        System.out.println("❌ Test Başarısız Oldu: " + result.getName());
        takeScreenshotToAllure();
    }

    // Ekran görüntüsü alıp Allure Report'a ekleyen metot:
    @Attachment(value = "Hata Ekran Görüntüsü", type = "image/png")
    public byte[] takeScreenshotToAllure() {
        return ((TakesScreenshot) DriverFactory.getDriver()).getScreenshotAs(OutputType.BYTES);
    }
}
```

---

### 3.8. Data-Driven Testing (@DataProvider ile Veri Güdümlü Test)

#### 🎯 Ne İşe Yarar?
Aynı test senaryosunu farklı veri setleriyle (örneğin 5 farklı arama kelimesi veya filtre kriteriyle) ayrı ayrı test metotları yazmadan tek metotla koşturmayı sağlar.

#### 🧩 Parça Kod Mantığı:
```java
package com.sahibinden.qa.dataprovider;

import org.testng.annotations.DataProvider;

public class SearchDataProvider {

    @DataProvider(name = "searchKeywords")
    public static Object[][] getSearchData() {
        return new Object[][] {
            {"BMW 320i"},
            {"Mercedes C200"},
            {"Audi A4"}
        };
    }
}
```
**Test sınıfında kullanımı:**
```java
@Test(dataProvider = "searchKeywords", dataProviderClass = SearchDataProvider.class)
public void testMultipleSearches(String keyword) {
    HomePage homePage = new HomePage(getDriver());
    homePage.handleCookies();
    SearchResultsPage resultsPage = homePage.search(keyword);
    Assert.assertTrue(resultsPage.getResultCount() > 0);
}
```

---

### 3.9. testng.xml ve Paralel Koşum Yapılandırması

#### 🎯 Ne İşe Yarar?
Hangi testlerin çalışacağını, hangi sırayla çalışacağını ve aynı anda kaç tarayıcı açılarak (paralel) çalışacağını yöneten merkezi XML dosyasıdır.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Sahibinden Test Suite" verbose="1" parallel="methods" thread-count="2">
    
    <listeners>
        <!-- Dinleyicimizi buraya bağlıyoruz -->
        <listener class-name="com.sahibinden.qa.listeners.TestListener"/>
    </listeners>

    <test name="Functional Tests">
        <packages>
            <!-- tests paketi altındaki tüm test sınıflarını otomatik bulur ve koşturur -->
            <package name="com.sahibinden.qa.tests.*"/>
        </packages>
    </test>
</suite>
```

---

## 4. Sahibinden.com Özelinde Zorluklar ve Çözümleri

### 1. Bot Tespiti (Anti-Bot) Önlemi:
Selenium açıldığında `navigator.webdriver = true` flag'i gönderir. Bunu engellemek için `DriverFactory`'deki `ChromeOptions`'a şu argüman eklenmelidir:
```java
options.addArguments("--disable-blink-features=AutomationControlled");
```

### 2. Dinamik/Değişken Elementler ve StaleElement Hatası:
Filtreleme yapıldığında sayfa tamamen yenilenmez, liste arka planda AJAX ile güncellenir. Bu yüzden arama sonuçlarını alırken `Thread.sleep` yerine listenin güncellendiğini bekleyen `WebDriverWait` kullanılmalıdır:
```java
wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.searchResultsItem")));
```

---

## 5. Altın Kurallar & Yapılmaması Gereken Hatalar

| ❌ Yanlış Yaklaşım | ✅ Doğru Yaklaşım | Neden? |
| :--- | :--- | :--- |
| `Thread.sleep(5000);` | `wait.until(ExpectedConditions...);` | Sleep testleri çok yavaşlatır ve kırılgan yapar. |
| Test sınıfı içine `By.id("btn")` yazmak | Locator'ı Page sınıfında tanımlamak | UI değiştiğinde tek bir yeri güncellemek için. |
| Page sınıfı içinde `Assert.assertEquals()` yapmak | Sadece Test sınıfında Assert yapmak | Sayfa sınıfları sadece iş yapar, test kararını Test katmanı verir. |
| `public static WebDriver driver;` | `ThreadLocal<WebDriver>` kullanmak | Testleri aynı anda paralel çalıştırabilmek için. |
| URL'i kod içine `driver.get("https://...")` yazmak | `config.baseUrl()` üzerinden okumak | Ortam değiştiğinde kodu yeniden derlememek için. |

---

🎯 **Tebrikler! Artık her yapının amacını ve kod mantığını biliyorsunuz. Kendi kodlarınızı bu şablonları rehber alarak adım adım geliştirebilirsiniz.**
