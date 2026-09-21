# 📱 Appium Mobil Tarayıcı (Mobile Web) Test Otomasyonu Rehberi ve Mimari Planı

Bu döküman, **qa-project-101** projemizde mevcut olan Web UI (Desktop) test senaryolarının **birebir aynısını**, kurumsal standartlardan ödün vermeden **Appium 2.x** ve **Selenium 4 / Java Client** altyapısıyla mobil tarayıcılarda (Android Chrome & iOS Safari) koşturmak için hazırlanan mimari plan, kurulum ve uygulama kılavuzudur.

---

## 📑 İçindekiler
1. [Mimari Genel Bakış ve Teknoloji Yığını](#1-mimari-genel-bakış-ve-teknoloji-yığını)
2. [Mevcut Web Test Senaryoları & Mobil Eşleşme Matrisi](#2-mevcut-web-test-senaryoları--mobil-eşleşme-matrisi)
3. [Ön Koşullar ve Ortam Kurulumu (Environment Setup)](#3-ön-koşullar-ve-ortam-kurulumu-environment-setup)
4. [Proje Mimarisi ve Paket Yapısı (Package Structure)](#4-proje-mimarisi-ve-paket-yapısı-package-structure)
5. [Bağımlılıklar ve Konfigürasyon Tasarımı (`pom.xml` & `config.properties`)](#5-bağımlılıklar-ve-konfigürasyon-tasarımı-pomxml--configproperties)
6. [Sürücü Yönetimi (Mobile Driver Factory & Capabilities)](#6-sürücü-yönetimi-mobile-driver-factory--capabilities)
7. [Base Sınıflar ve Mobil Page Object Model (POM) Yaklaşımı](#7-base-sınıflar-ve-mobil-page-object-model-pom-yaklaşımı)
8. [Mobil Test Sınıfları ve Senaryo Tasarımları (1:1 Parity)](#8-mobil-test-sınıfları-ve-senaryo-tasarımları-11-parity)
9. [TestNG Suite ve Koşum Senaryoları (Execution & CI/CD)](#9-testng-suite-ve-koşum-senaryoları-execution--cicd)
10. [Adım Adım Geliştirme Yol Haritası (Roadmap)](#10-adım-adım-geliştirme-yol-haritası-roadmap)

---

## 1. Mimari Genel Bakış ve Teknoloji Yığını

Mevcut projemizdeki **ThreadLocal WebDriver**, **Page Object Model (POM)**, **Allure Reporting**, **Log4j2**, **AssertJ** ve **Owner (Type-safe Config)** mimarisi mobil tarayıcı otomasyonunda da aynen korunacaktır.

| Katman | Teknoloji | Açıklama |
|---|---|---|
| **Test Runner** | TestNG 7.10+ | Paralel koşum, test lifecycle ve veri yönetimi |
| **Mobile Engine** | Appium 2.x | Mobil cihaz iletişimi ve W3C WebDriver uyumluluğu |
| **Appium Client** | `io.appium:java-client:9.2.2` | Appium Java SDK (Selenium 4.21 uyumlu) |
| **Android Driver** | `UiAutomator2` + `ChromeDriver` | Android emülatör/fiziksel cihazlarda mobil Chrome testi |
| **iOS Driver** | `XCUITest` + `Safari` | iOS Simulator/fiziksel cihazlarda mobil Safari testi |
| **Config Engine** | `org.aeonbits.owner` | Ortam ve cihaz bazlı type-safe konfigürasyon |
| **Raporlama** | Allure Report 2.27+ | Mobil ekran görüntüleri, loglar ve adım bazlı raporlama |
| **Loglama** | Log4j2 + SLF4J | Kurumsal konsol ve dosya bazlı loglama |

---

## 2. Mevcut Web Test Senaryoları & Mobil Eşleşme Matrisi

Web tarafında çalışan **5 ana test paketi** ve **11 test senaryosu**, mobil web ortamında aşağıdaki şekilde 1:1 uygulanacaktır:

| Web Test Sınıfı | Mobil Test Sınıfı | Senaryo Adı | Mobil Özel Durum / Aksiyon |
|---|---|---|---|
| `SearchTests` | `MobileSearchTests` | `testSearchProducts` | Mobil arama ikonu tıklanır, arama çubuğu açılır, sonuç sayısı ve ilk ürün doğrulanır. |
| `SearchTests` | `MobileSearchTests` | `testSearchProductsWithDataProvider` | Data-driven JSON araması (`search_terms.json`). |
| `SearchTests` | `MobileSearchTests` | `testSearchNonExistingProduct` | Geçersiz arama terimi, "sonuç bulunamadı" ekranı doğrulaması. |
| `SearchFilterTests` | `MobileSearchFilterTests` | `testFilterByBrand` | Mobil filtreleme butonuna/drawer'a tıklanır, marka filtresi seçilir ve sonuçlar onaylanır. |
| `SearchFilterTests` | `MobileSearchFilterTests` | `testSortByLowestPrice` | Mobil sıralama menüsü/dropdown açılır, "En düşük fiyat" seçilir, artan fiyat doğrulanır. |
| `SearchFilterTests` | `MobileSearchFilterTests` | `testFilterByPriceRange` | Mobil filtre drawer'ında Min-Max fiyat aralığı girilir ve filtrelenir. |
| `CartTests` | `MobileCartTests` | `testAddProductToCart` | Ürün aranır, PDP'ye girilir, sepete eklenir, mobil alt bardan/header'dan sepete gidilir, ürün doğrulanır. |
| `CartTests` | `MobileCartTests` | `testRemoveProductFromCart` | Sepetteki ürün silinir, "Sepetiniz Boş" ekranı doğrulanır. |
| `ProductDetailTests` | `MobileProductDetailTests` | `shouldUpdateProductWhenVariantSelected` | Mobil PDP'de varyant (renk/hafıza) seçilir, fiyat/durum güncellenmesi doğrulanır. |
| `ProductDetailTests` | `MobileProductDetailTests` | `shouldRequireLoginWhenGuestAddsToWishlist` | Misafir kullanıcı olarak ❤️ butonuna basılır, login yönlendirmesi doğrulanır. |
| `ProductDetailTests` | `MobileProductDetailTests` | `shouldDisplayReviewsWhenReviewsTabClicked` | Mobil PDP'de Değerlendirmeler sekmesine scroll edilir ve tıklanarak yorumlar doğrulanır. |
| `LoginTests` | `MobileLoginTests` | `shouldShowErrorForInvalidEmailFormat` | Mobil login sayfasına gidilir, geçersiz e-posta girilir, hata uyarısı doğrulanır. |
| `LoginTests` | `MobileLoginTests` | `shouldShowErrorForEmptyEmailSubmission` | Boş e-posta ile form gönderilir, zorunlu alan uyarısı doğrulanır. |

---

## 3. Ön Koşullar ve Ortam Kurulumu (Environment Setup)

Mobil tarayıcı testlerinin lokalde veya CI/CD sunucularında koşabilmesi için gerekli altyapı:

### 3.1. Appium 2.x Sunucusu Kurulumu
```bash
# Appium 2.x kurulumu
npm install -g appium

# Gerekli Appium sürücülerinin kurulması
appium driver install uiautomator2
appium driver install xcuitest

# Kurulum doğrulaması
appium driver list
```

### 3.2. Android Emülatör & ChromeDriver Yapılandırması
* **Android Studio & SDK**: `ANDROID_HOME` ve `platform-tools` (adb) environment variable'a eklenmelidir.
* **Otomatik ChromeDriver Eşleme**: Appium'un mobil Chrome sürümüyle uyumlu ChromeDriver'ı otomatik indirmesi için capability:
  ```json
  "appium:chromedriver_autodownload": true
  ```

### 3.3. Appium Sunucusunu Başlatma
```bash
appium --address 127.0.0.1 --port 4723 --allow-cors --relaxed-security
```

---

## 4. Proje Mimarisi ve Paket Yapısı (Package Structure)

Projede var olan modüler yapı bozulmadan mobil sınıflar organize edilecektir:

```text
src/
├── main/java/com/hepsiburada/qa/
│   ├── config/
│   │   ├── ConfigReader.java              # Mevcut Web Config
│   │   └── MobileConfigReader.java        # [YENİ] Mobil Özel Config (Owner)
│   ├── drivers/
│   │   ├── BrowserType.java               # Mevcut Web Browsers
│   │   ├── DriverFactory.java             # Mevcut Web DriverFactory
│   │   ├── MobileBrowserType.java         # [YENİ] CHROME_MOBILE, SAFARI_MOBILE
│   │   └── MobileDriverFactory.java       # [YENİ] ThreadLocal<AppiumDriver> Yönetimi
│   └── pages/
│       ├── base/
│       │   ├── BasePage.java              # Mevcut BasePage
│       │   └── MobileBasePage.java        # [YENİ] Touch, Swipe, Mobile Scroll destekli BasePage
│       └── mobile/                        # [YENİ] Mobil Sayfa Nesneleri
│           ├── home/
│           │   ├── MobileHomePage.java
│           │   └── components/
│           │       └── MobileHeaderComponent.java
│           ├── search/
│           │   ├── MobileSearchResultPage.java
│           │   └── MobileFilterDrawerComponent.java
│           ├── pdp/
│           │   └── MobileProductDetailPage.java
│           ├── cart/
│           │   └── MobileCartPage.java
│           └── login/
│               └── MobileLoginPage.java
│
└── test/java/com/hepsiburada/qa/
    ├── base/
    │   ├── BaseTest.java                  # Mevcut Web BaseTest
    │   └── MobileBaseTest.java            # [YENİ] Mobil TestNG Lifecycle & Setup/Teardown
    └── tests/mobile/                      # [YENİ] Birebir Web ile Eşleşen Mobil Testler
        ├── search/
        │   ├── MobileSearchTests.java
        │   └── MobileSearchFilterTests.java
        ├── cart/
        │   └── MobileCartTests.java
        ├── pdp/
        │   └── MobileProductDetailTests.java
        └── login/
            └── MobileLoginTests.java

src/test/resources/
├── config/
│   ├── config.properties                 # Web ayarları
│   └── mobile-config.properties          # [YENİ] Mobil cihaz ve Appium ayarları
└── suites/
    ├── mobile-regression-tests.xml       # [YENİ] Mobil Tüm Regresyon Paketi
    └── mobile-smoke-tests.xml            # [YENİ] Mobil Kritik Akış Paketi
```

---

## 5. Bağımlılıklar ve Konfigürasyon Tasarımı (`pom.xml` & `config.properties`)

### 5.1. `pom.xml` Bağımlılık Eklemesi (Planlanan)
Mevcut Selenium 4.21.0 sürümümüz ile tam uyumlu resmi **Appium Java Client 9.2.2**:

```xml
<!-- ==================== APPIUM CLIENT (MOBILE BROWSER) ==================== -->
<dependency>
    <groupId>io.appium</groupId>
    <artifactId>java-client</artifactId>
    <version>9.2.2</version>
</dependency>
```

### 5.2. `src/test/resources/config/mobile-config.properties` İçeriği
```properties
# Appium Server Configuration
appium.server.url=http://127.0.0.1:4723/
mobile.base.url=https://www.hepsiburada.com

# Platform Selection: ANDROID or IOS
mobile.platform=ANDROID

# Android Chrome Capabilities
mobile.android.device.name=Pixel_7_API_34
mobile.android.platform.version=14.0
mobile.android.automation.name=UiAutomator2
mobile.android.browser.name=Chrome
mobile.android.chromedriver.autodownload=true
mobile.android.headless=false

# iOS Safari Capabilities
mobile.ios.device.name=iPhone 15 Pro
mobile.ios.platform.version=17.4
mobile.ios.automation.name=XCUITest
mobile.ios.browser.name=Safari

# Mobile Execution Parameters
mobile.implicit.wait=10
mobile.explicit.wait=15
```

### 5.3. `MobileConfigReader.java` (Type-Safe Interface)
```java
@Config.Sources({"classpath:config/mobile-config.properties", "system:properties"})
public interface MobileConfigReader extends Config {
    @Key("appium.server.url")
    @DefaultValue("http://127.0.0.1:4723/")
    String appiumServerUrl();

    @Key("mobile.base.url")
    @DefaultValue("https://www.hepsiburada.com")
    String mobileBaseUrl();

    @Key("mobile.platform")
    @DefaultValue("ANDROID")
    String platform();

    @Key("mobile.android.device.name")
    String androidDeviceName();

    @Key("mobile.android.automation.name")
    @DefaultValue("UiAutomator2")
    String androidAutomationName();

    @Key("mobile.android.browser.name")
    @DefaultValue("Chrome")
    String androidBrowserName();

    @Key("mobile.explicit.wait")
    @DefaultValue("15")
    int explicitWait();
}
```

---

## 6. Sürücü Yönetimi (Mobile Driver Factory & Capabilities)

### 6.1. `MobileBrowserType.java`
```java
public enum MobileBrowserType {
    ANDROID_CHROME {
        @Override
        public BaseOptions<?> getOptions(MobileConfigReader config) {
            UiAutomator2Options options = new UiAutomator2Options();
            options.setPlatformName("Android");
            options.setDeviceName(config.androidDeviceName());
            options.withBrowserName("Chrome");
            options.setAutomationName("UiAutomator2");
            options.setCapability("appium:chromedriver_autodownload", true);
            options.setCapability("appium:newCommandTimeout", 300);
            options.setCapability("appium:noReset", false);
            return options;
        }

        @Override
        public AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config) {
            return new AndroidDriver(appiumServerUrl, getOptions(config));
        }
    },

    IOS_SAFARI {
        @Override
        public BaseOptions<?> getOptions(MobileConfigReader config) {
            XCUITestOptions options = new XCUITestOptions();
            options.setPlatformName("iOS");
            options.setDeviceName("iPhone 15 Pro");
            options.withBrowserName("Safari");
            options.setAutomationName("XCUITest");
            options.setCapability("appium:newCommandTimeout", 300);
            return options;
        }

        @Override
        public AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config) {
            return new IOSDriver(appiumServerUrl, getOptions(config));
        }
    };

    public abstract BaseOptions<?> getOptions(MobileConfigReader config);
    public abstract AppiumDriver createDriver(URL appiumServerUrl, MobileConfigReader config);
}
```

### 6.2. `MobileDriverFactory.java` (ThreadLocal)
```java
public class MobileDriverFactory {
    private static final ThreadLocal<AppiumDriver> driverPool = new ThreadLocal<>();

    private MobileDriverFactory() {}

    public static void initDriver(MobileBrowserType browserType, MobileConfigReader config) {
        if (driverPool.get() == null) {
            try {
                URL serverUrl = new URL(config.appiumServerUrl());
                AppiumDriver driver = browserType.createDriver(serverUrl, config);
                driverPool.set(driver);
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid Appium Server URL: " + config.appiumServerUrl(), e);
            }
        }
    }

    public static AppiumDriver getDriver() {
        return driverPool.get();
    }

    public static void quitDriver() {
        if (driverPool.get() != null) {
            try {
                driverPool.get().quit();
            } finally {
                driverPool.remove();
            }
        }
    }
}
```

---

## 7. Base Sınıflar ve Mobil Page Object Model (POM) Yaklaşımı

Mobil webde ekran boyutları ve responsive davranışlar nedeniyle butonlar hamburger menü arkasına geçebilir, filtreler modal (drawer) olarak açılır ve scroll mekanizmaları touch/swipe gerektirebilir.

### 7.1. `MobileBasePage.java`
```java
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
        log.info("Mobil elemente tıklanıyor: {}", locator);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
        scrollIntoView(element);
        element.click();
    }

    protected void sendKeys(By locator, String text) {
        log.info("Mobil alana '{}' yazılıyor: {}", text, locator);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        element.clear();
        element.sendKeys(text);
    }

    protected void scrollIntoView(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
        } catch (Exception ignored) {}
    }

    // Mobil swipe/touch helper
    public void scrollDown() {
        Dimension size = driver.manage().window().getSize();
        int startX = size.width / 2;
        int startY = (int) (size.height * 0.75);
        int endY = (int) (size.height * 0.25);
        
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);
        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(600), PointerInput.Origin.viewport(), startX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));
        driver.perform(Collections.singletonList(swipe));
    }
}
```

### 7.2. `MobileBaseTest.java`
```java
public abstract class MobileBaseTest {
    protected MobileConfigReader config;

    @BeforeMethod
    public void setUp() {
        config = ConfigFactory.create(MobileConfigReader.class);
        MobileBrowserType browserType = MobileBrowserType.valueOf(
            config.platform().toUpperCase() + "_" + config.androidBrowserName().toUpperCase()
        );
        MobileDriverFactory.initDriver(browserType, config);
        getDriver().get(config.mobileBaseUrl());
    }

    @AfterMethod
    public void tearDown() {
        MobileDriverFactory.quitDriver();
    }

    public AppiumDriver getDriver() {
        return MobileDriverFactory.getDriver();
    }
}
```

---

## 8. Mobil Test Sınıfları ve Senaryo Tasarımları (1:1 Parity)

Web tarafındaki tüm senaryolar birebir aynı iş kurallarıyla mobil sayfa nesneleri üzerinden işletilecektir:

### 8.1. `MobileSearchTests.java`
* **`testSearchProducts`**: Mobil ana sayfada arama ikonuna basılır, "Laptop" yazılır, Enter/Arama tetiklenir, sonuç sayısı (`> 0`) ve ilk ürün başlığının dolu olduğu doğrulanır.
* **`testSearchProductsWithDataProvider`**: `SearchDataProvider` kullanılarak çoklu anahtar kelimeler ile mobil arama koşturulur.
* **`testSearchNonExistingProduct`**: Anlamsız metin girilerek mobil sonuç bulunamadı uyarısı veya 0 ürün listelenmesi doğrulanır.

### 8.2. `MobileSearchFilterTests.java`
* **`testFilterByBrand`**: "Laptop" aramasından sonra mobil "Filtrele" butonuna tıklanır, marka modalından "Apple" seçilir, filtre uygulanır, listelenen ilk ürünlerin Apple/MacBook içerdiği doğrulanır.
* **`testSortByLowestPrice`**: "Sırala" menüsü açılır, "En Düşük Fiyat" seçilir, gelen ilk 2 ürünün fiyatlarının artan sırada (`price[0] <= price[1]`) olduğu kontrol edilir.
* **`testFilterByPriceRange`**: Fiyat aralığı filtresi girilerek sonuçların aralıkta olduğu teyit edilir.

### 8.3. `MobileCartTests.java`
* **`testAddProductToCart`**: Ürün aranır, mobil PDP'ye girilir, "Sepete Ekle" butonuna basılır, alt navigasyondan veya sepet bildiriminden sepete gidilir, ürünün sepette olduğu doğrulanır.
* **`testRemoveProductFromCart`**: Sepetteki ürün "Sil" butonuyla kaldırılır, "Sepetiniz şu an boş" mesajı doğrulanır.

### 8.4. `MobileProductDetailTests.java`
* **`shouldUpdateProductWhenVariantSelected`**: Çok varyantlı bir üründe (örn: iPhone) hafıza/renk kutucuğuna tıklanır, fiyat ve başlığın dinamik güncellendiği doğrulanır.
* **`shouldRequireLoginWhenGuestAddsToWishlist`**: Misafir kullanıcı olarak ❤️ butonuna basıldığında mobil login modalı veya login sayfası URL yönlendirmesi doğrulanır.
* **`shouldDisplayReviewsWhenReviewsTabClicked`**: "Değerlendirmeler" sekmesine inilip tıklanır, puanlama ve yorum kartlarının görünür olduğu doğrulanır.

### 8.5. `MobileLoginTests.java`
* **`shouldShowErrorForInvalidEmailFormat`**: Mobil giriş sayfasına gidilir, geçersiz formatta (`gecersizmail@`) e-posta yazılır, "Giriş yap" butonuna basılır ve hata mesajı doğrulanır.
* **`shouldShowErrorForEmptyEmailSubmission`**: Boş form gönderilir ve zorunlu alan doğrulaması yapılır.

---

## 9. TestNG Suite ve Koşum Senaryoları (Execution & CI/CD)

### 9.1. `src/test/resources/suites/mobile-regression-tests.xml`
```xml
<!DOCTYPE suite SYSTEM "https://testng.org/testng-1.0.dtd">
<suite name="Mobile Browser Regression Test Suite" parallel="classes" thread-count="2" verbose="1">
    <listeners>
        <listener class-name="com.hepsiburada.qa.listeners.TestListener"/>
    </listeners>

    <test name="Mobile Web - Android Chrome Suite">
        <parameter name="platform" value="ANDROID"/>
        <classes>
            <class name="com.hepsiburada.qa.tests.mobile.search.MobileSearchTests"/>
            <class name="com.hepsiburada.qa.tests.mobile.search.MobileSearchFilterTests"/>
            <class name="com.hepsiburada.qa.tests.mobile.pdp.MobileProductDetailTests"/>
            <class name="com.hepsiburada.qa.tests.mobile.cart.MobileCartTests"/>
            <class name="com.hepsiburada.qa.tests.mobile.login.MobileLoginTests"/>
        </classes>
    </test>
</suite>
```

### 9.2. Maven CLI Koşum Komutları
```bash
# Sadece Mobil Regresyon Testlerini Koşmak İçin
mvn clean test -DsuiteXmlFile=src/test/resources/suites/mobile-regression-tests.xml

# Belirli Bir Cihaz / Platform Üzerinde Koşmak İçin
mvn clean test \
  -DsuiteXmlFile=src/test/resources/suites/mobile-regression-tests.xml \
  -Dmobile.platform=ANDROID \
  -Dmobile.android.device.name="Pixel_7_API_34"

# Allure Raporunu Üretmek ve Açmak İçin
mvn allure:serve
```

---

## 10. Adım Adım Geliştirme Yol Haritası (Roadmap)

Kullanıcı onayı alındıktan sonra sırasıyla uygulanacak aşamalar:

1. 📦 **Aşama 1 - Bağımlılık & Yapılandırma:**
   - `pom.xml` içerisine `io.appium:java-client` bağımlılığının eklenmesi.
   - `mobile-config.properties` ve `MobileConfigReader.java` sınıflarının oluşturulması.

2. 🚗 **Aşama 2 - Driver ve Base Katmanı:**
   - `MobileBrowserType.java` ve `MobileDriverFactory.java` (ThreadLocal) sınıflarının geliştirilmesi.
   - `MobileBasePage.java` (Scroll/Touch aksiyonları) ve `MobileBaseTest.java` sınıflarının kodlanması.

3. 📱 **Aşama 3 - Mobile Page Object Model (POM):**
   - `MobileHomePage`, `MobileSearchResultPage`, `MobileProductDetailPage`, `MobileCartPage`, `MobileLoginPage` sınıflarının oluşturulması.

4. 🧪 **Aşama 4 - Test Sınıflarının Yazımı (1:1 Web Eşleşmesi):**
   - Arama, Filtreleme, PDP, Sepet ve Giriş testlerinin mobil karşılıklarının kodlanması.

5. ⚙️ **Aşama 5 - TestNG Suite & Doğrulama:**
   - `mobile-regression-tests.xml` ve `mobile-smoke-tests.xml` dosyalarının oluşturulması ve Allure entegrasyonuyla doğrulanması.

---

> [!IMPORTANT]
> **Durum:** Bu döküman ve mimari plan eksiksiz olarak `docs/APPIUM_MOBILE_BROWSER_TESTING_GUIDE.md` dosyasına yazılmıştır. Kullanıcı onayı gelene kadar projede hiçbir kod geliştirilmeyecektir.
