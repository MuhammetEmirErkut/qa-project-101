# 🔐 Kullanıcı Girişi ve Negatif Login Testleri Rehberi (LoginPage)

Bu rehberde, Hepsiburada üzerinde **Kullanıcı Girişi (Login)** akışına yönelik **Negatif Test Senaryolarını** Page Object Model (POM) mimarisine uygun şekilde adım adım nasıl geliştireceğinizi bulacaksınız:

1. **Geçersiz E-posta Formatı Testi (Invalid Email Format):**
   * **Senaryo:** E-posta alanına hatalı formatta bir metin (örn: `test@` veya `gecersizmail`) yazılıp "Giriş Yap / Devam Et" butonuna tıklanır.
   * **Doğrulama:** Ekranda *"Geçerli bir e-posta adresi giriniz"* veya benzeri format validasyon uyarısının belirdiğinin doğrulanması.
2. **Boş Form Gönderimi Testi (Empty Email Submission):**
   * **Senaryo:** E-posta alanı tamamen boş bırakılarak butona tıklanır.
   * **Doğrulama:** Zorunlu alan uyarı mesajının (*"Lütfen e-posta adresinizi giriniz"* veya *"Bu alan zorunludur"*) görüntülendiğinin doğrulanması.

---

## 📚 1. Bölüm: Öğrenilecek Temel Kavramlar

### 1.1. Negatif Testler ve İstemci Taraflı Validasyonlar (Negative Testing & Client Validation)
* **Kavram:** Negatif testler, sistemin beklenmeyen veya hatalı kullanıcı girdileri karşısında çökmeden, kullanıcıya doğru ve anlaşılır hata mesajları verip vermediğini denetler.
* **Test Yaklaşımı:** Hata mesajlarının tam metni (exact text) yerine esnek metin kontrolleri (`contains("geçerli")` veya `contains("e-posta")`) kullanılır; böylece frontend metin revizyonlarında testler kırılmaz.

### 1.2. İki Adımlı Giriş Mimarisi (Two-Step Authentication Flow)
* **Kavram:** Modern web uygulamaları (Google, Hepsiburada vb.) önce kullanıcı e-postasını/telefonunu alır, hesabı tespit ettikten sonra ikinci ekranda şifre veya OTP kodunu ister.
* **Test Yaklaşımı:** İlk adımda e-posta doğrulaması (email step) başarısız olduğunda sistem şifre ekranına geçmemeli, aynı ekranda input altında veya alert kutusunda hata göstermelidir.

### 1.3. Input Temizleme Stratejisi (Safe Input Clearing)
* **Problem:** Selenium'un standart `element.clear()` metodu React/Vue gibi dinamik formlarda bazen state'i sıfırlamaz ve eski karakterler kalabilir.
* **Çözüm:** Klavye kısayolu ile tüm metni seçip silme (`sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)`) veya JavaScript ile value sıfırlama tercih edilir.

---

## 🛠️ 2. Bölüm: `LoginPage` ve `HomePage` Sınıflarının Hazırlanması

### 🔹 1. Adım: `HomePage.java` İçine Login Yönlendirmesi Eklenmesi

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/home/HomePage.java`

`HomePage` sınıfına ana sayfadan giriş sayfasına geçiş sağlayan metodu ekliyoruz:

```java
// ==================== LOGIN / ACCOUNT LOCATORS ====================
private final By myAccountButton = By.cssSelector("#myAccount, [data-test-id='account'], div[class*='account-user']");
private final By loginDropdownOption = By.cssSelector("#login, a[href*='giris.hepsiburada.com'], [data-test-id='login-button'], a[href*='login']");

/**
 * Giriş Yap sayfasına yönlendirir.
 */
@Step("Navigate to login page")
public LoginPage goToLoginPage() {
    handleCookies();
    try {
        WebElement accountBtn = wait.until(ExpectedConditions.elementToBeClickable(myAccountButton));
        // Hesabım butonuna hover veya click yap
        new org.openqa.selenium.interactions.Actions(driver).moveToElement(accountBtn).perform();
        Thread.sleep(500);
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(loginDropdownOption));
        jsClick(loginBtn);
    } catch (Exception e) {
        // Doğrudan Login URL'ine git (Hızlı ve stabil fallback)
        driver.get("https://giris.hepsiburada.com");
    }
    return new LoginPage(driver);
}
```

---

### 🔹 2. Adım: `LoginPage.java` Sınıfının Oluşturulması

**Dosya:** `src/main/java/com/hepsiburada/qa/pages/login/LoginPage.java`

Giriş ekranındaki elementleri ve işlemleri yöneten yeni `LoginPage` sayfa nesnesini oluşturuyoruz:

```java
package com.hepsiburada.qa.pages.login;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.hepsiburada.qa.pages.base.BasePage;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {

    // ==================== LOCATORS ====================
    private final By emailInput = By.cssSelector(
        "input#txtUserName, input[name='username'], input[type='email'], input[data-test-id='email-input'], input[placeholder*='e-posta' i]"
    );

    private final By loginButton = By.cssSelector(
        "button#btnLogin, button[type='submit'], button[data-test-id='login-button'], button[class*='login']"
    );

    private final By errorMessage = By.xpath(
        "//*[contains(., 'Geçerli bir e-posta') or contains(., 'girmelisiniz') or contains(., 'giriniz') or contains(., 'zorunlu')] | " +
        "//div[contains(@class, 'error') or contains(@class, 'Error') or contains(@class, 'helperText') or contains(@class, 'HelperText')] | " +
        "//p[contains(@class, 'error') or contains(@class, 'helper')] | " +
        "//span[contains(@class, 'error') or contains(@class, 'helper')]"
    );

    public LoginPage(WebDriver driver) {
        super(driver);
        waitForLoginPageLoad();
    }

    public void waitForLoginPageLoad() {
        try {
            new WebDriverWait(driver, Duration.ofSeconds(15)).until(ExpectedConditions.or(
                ExpectedConditions.presenceOfElementLocated(emailInput),
                ExpectedConditions.presenceOfElementLocated(loginButton)
            ));
        } catch (Exception ignored) {}
    }

    @Step("Enter email: {email}")
    public LoginPage enterEmail(String email) {
        WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        input.click();
        input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        input.sendKeys(email);
        return this;
    }

    @Step("Clear email input field")
    public LoginPage clearEmailInput() {
        try {
            WebElement input = wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
            input.click();
            input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
        } catch (Exception ignored) {}
        return this;
    }

    @Step("Click login submit button")
    public LoginPage clickLoginButton() {
        try {
            WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(loginButton));
            jsClick(btn);
        } catch (Exception e) {
            WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(loginButton));
            jsClick(btn);
        }

        try {
            Thread.sleep(1200); // Validasyon mesajının DOM'a yansımasını bekle
        } catch (InterruptedException ignored) {}

        return this;
    }

    @Step("Get validation error message text")
    public String getErrorMessage() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement errorEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return errorEl.getText().trim();
        } catch (Exception e) {
            try {
                return driver.findElement(By.xpath("//*[contains(text(), 'Geçerli bir e-posta')]")).getText().trim();
            } catch (Exception ignored) {}
        }
        return "";
    }

    @Step("Verify validation error message is displayed")
    public boolean isErrorMessageDisplayed() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            WebElement errorEl = shortWait.until(ExpectedConditions.visibilityOfElementLocated(errorMessage));
            return errorEl != null && errorEl.isDisplayed();
        } catch (Exception e) {
            try {
                WebElement fallback = driver.findElement(By.xpath("//*[contains(text(), 'Geçerli bir e-posta')]"));
                return fallback.isDisplayed();
            } catch (Exception ignored) {
                return false;
            }
        }
    }
}
```

---

## 🧪 3. Bölüm: `LoginTests` Test Sınıfının Geliştirilmesi

**Dosya:** `src/test/java/com/hepsiburada/qa/tests/login/LoginTests.java`

İki temel negatif senaryoyu içeren `LoginTests` sınıfını oluşturuyoruz:

```java
package com.hepsiburada.qa.tests.login;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.hepsiburada.qa.base.BaseTest;
import com.hepsiburada.qa.pages.home.HomePage;
import com.hepsiburada.qa.pages.login.LoginPage;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;

@Epic("Authentication Flow")
@Feature("Negative Login Validations")
public class LoginTests extends BaseTest {

    @Test(priority = 1, description = "Login should fail with validation message for invalid email format")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Invalid Email Format")
    @Description("Verify that an error message is displayed when entering an invalid email format (e.g. test@ or gecersizmail)")
    public void shouldShowErrorForInvalidEmailFormat() {
        // 1. Initialize HomePage and handle cookies
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Login Page
        LoginPage loginPage = homePage.goToLoginPage();

        // 3. Enter invalid email format (e.g. 'gecersizmail' or 'test@')
        loginPage.enterEmail("gecersizmail@");

        // 4. Click login button to submit
        loginPage.clickLoginButton();

        // 5. Verify validation error message is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Validation error message should be displayed for invalid email format");

        // 6. Verify error text contains relevant guidance
        String errorMsg = loginPage.getErrorMessage().toLowerCase();
        Assert.assertTrue(
            errorMsg.contains("geçerli") || errorMsg.contains("e-posta") || errorMsg.contains("giriniz") || errorMsg.contains("format"),
            "Error message should indicate invalid email format, but found: " + errorMsg
        );
    }

    @Test(priority = 2, description = "Login should show required field validation for empty form submission")
    @Severity(SeverityLevel.CRITICAL)
    @Story("Empty Form Submission")
    @Description("Verify that a required field warning is displayed when submitting empty email input")
    public void shouldShowErrorForEmptyEmailSubmission() {
        // 1. Initialize HomePage and handle cookies
        HomePage homePage = new HomePage(getDriver());
        homePage.handleCookies();

        // 2. Navigate to Login Page
        LoginPage loginPage = homePage.goToLoginPage();

        // 3. Ensure email field is empty
        loginPage.clearEmailInput();

        // 4. Click login button without entering any credentials
        loginPage.clickLoginButton();

        // 5. Verify required field warning is displayed
        Assert.assertTrue(loginPage.isErrorMessageDisplayed(),
            "Required field warning should be displayed when submitting empty email");
    }
}
```

---

## 🚀 4. Bölüm: Testleri Çalıştırma ve Raporlama

### 4.1. Terminalden Testlerin Koşulması

Yalnızca Login testlerini çalıştırmak için:
```bash
mvn test -Dtest=LoginTests
```

Tekil senaryo çalıştırmak için (Örn: Geçersiz format testi):
```bash
mvn test -Dtest=LoginTests#shouldShowErrorForInvalidEmailFormat
```

Tüm test paketini çalıştırmak için:
```bash
mvn clean test
```

### 4.2. Allure Raporunu Başlatma

```bash
mvn allure:serve
```

---

## 💡 5. Bölüm: Negatif Testler İçin En İyi Pratikler (Best Practices)

1. **Esnek Assertion (Resilient Assertions):** UI metinleri ("Geçerli bir e-posta giriniz" vs "Lütfen geçerli bir e-posta adresi yazınız") zamanla güncellenebilir. `Assert.assertEquals` yerine `errorText.contains("geçerli")` kullanmak test stabilitesini artırır.
2. **Doğrudan URL Fallback'i:** `HomePage` üzerindeki açılır menü (Hover) zaman zaman animasyon gecikmesi yaşayabilir. Bu nedenle `goToLoginPage()` metodu menüyü açamazsa doğrudan `https://giris.hepsiburada.com` adresine giderek testi bloklanmaktan kurtarır.
3. **Klavye ile Temizleme:** `input.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE)` kullanımı, modern JavaScript framework'lerinin (React/Next.js) `onChange` event'lerini tetikleyerek inputun tamamen temizlenmesini garanti eder.
