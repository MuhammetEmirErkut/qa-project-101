# 🤝 QA Automation Platform - Katkı Sağlama Rehberi (Contributing Guidelines)

Bu belge, **qa-project-101** projesine katkıda bulunmak isteyen QA / SDET ve yazılım mühendisleri için kodlama standartlarını, Page Object prensiplerini ve Pull Request (PR) süreçlerini tanımlar.

---

## 🏗️ 1. Mimari Prensipler

1. **Page Object Model (POM):**
   - Her sayfa veya UI bileşeni `src/main/java/com/hepsiburada/qa/pages/` altında modellenmelidir.
   - Tüm Page Object'ler `BasePage` sınıfından türetilmelidir.
   - Sayfa sınıfları içerisinde assertion (`Assert.assertTrue`) **bulunmamalıdır**. Doğrulamalar yalnızca Test sınıflarında (`src/test/java/...`) yapılmalıdır.
   - Sayfa eylemleri, yeni bir sayfaya yönlendiriyorsa o sayfanın nesnesini döndürmelidir (Fluent Page Pattern).

2. **Dinamik Beklemeler (Dynamic Waits):**
   - Kod içinde **kesinlikle `Thread.sleep` kullanılmamalıdır**.
   - Beklemeler `WebDriverWait` ve `ExpectedConditions` ile koşula bağlı yapılmalıdır.
   - Sayfa yüklenme kontrolleri için `waitForPageToLoad()` metodu kullanılmalıdır.

3. **REST API Testleri:**
   - Yeni API testleri `src/test/java/com/hepsiburada/qa/api/` altında `BaseApiTest` sınıfından türetilmelidir.
   - Request ve Response doğrulamalarında `RestAssured` ve `AssertJ` kullanılmalıdır.

4. **Test Verisi Ayrımı (Data-Driven):**
   - Test verileri kod içine hardcoded yazılmamalı; `src/test/resources/testdata/` altındaki JSON/CSV dosyalarından `JsonDataReader` ile okunmalıdır.

---

## 🌿 2. Git Branching Stratejisi

- `main` / `master`: Stabil, her an CI üzerinde koşan ana üretim dalı.
- Dal İsimlendirme Formatı:
  - `feat/HB-123-add-payment-tests`
  - `fix/HB-456-fix-cart-locator`
  - `refactor/HB-789-upgrade-basepage`

---

## 🔍 3. Kod Kalitesi ve Yerel Doğrulama

Bir PR açmadan önce yerel ortamınızda aşağıdaki adımları çalıştırarak kodun temiz olduğunu doğrulayın:

```bash
# 1. Derleme ve Bağımlılık Kontrolü
mvn test-compile

# 2. Kod Standartları Kontrolü
mvn checkstyle:check

# 3. API ve Duman Testlerini Koşturma
make test-api
make test-ui
```

---

## 📋 4. Pull Request (PR) Kontrol Listesi

- [ ] Kod standartlarına uyuldu mu (Checkstyle hatasız mı)?
- [ ] Yeni testler Allure `@Epic`, `@Feature`, `@Severity` ve `@Description` etiketlerine sahip mi?
- [ ] Statik bekleme (`Thread.sleep`) içermediğinden emin olundu mu?
- [ ] Gerekli TestNG Suite XML (`smoke-tests.xml` / `regression-tests.xml` / `api-tests.xml`) güncellendi mi?
- [ ] CI pipeline (GitHub Actions) başarıyla geçti mi?
