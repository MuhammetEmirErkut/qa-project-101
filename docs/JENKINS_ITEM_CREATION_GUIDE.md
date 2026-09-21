# 🤖 Jenkins UI Item (Job) Oluşturma ve Yapılandırma Rehberi

> **Hedef:** Jenkins CI/CD Controller üzerinde UI Testleri, JMeter Performans Testleri ve Smoke Kontrolleri için bağımsız Pipeline Job'ları açmak.  
> **Erişim:** [http://localhost:8080](http://localhost:8080)  
> **Konum:** Tüm pipeline dosyalarımız `ci/pipelines/` klasörü altındadır.

---

## 📑 İçindekiler
1. [Ön Hazırlık: İlk Giriş ve Gerekli Eklentiler (Plugins)](#1-ön-hazırlık-ilk-giriş-ve-gerekli-eklentiler-plugins)
2. [Global Araç Yapılandırması (Global Tool Configuration)](#2-global-araç-yapılandırması-global-tool-configuration)
3. [Item 1: UI Test Otomasyon Hattı (`01-UI-Regression-Pipeline`)](#3-item-1-ui-test-otomasyon-hattı-01-ui-regression-pipeline)
4. [Item 2: JMeter Performans & Yük Test Hattı (`02-Performance-Testing-Pipeline`)](#4-item-2-jmeter-performans--yük-test-hattı-02-performance-testing-pipeline)
5. [Item 3: Hızlı Smoke & Sağlık Kontrol Hattı (`03-Smoke-Verification-Pipeline`)](#5-item-3-hızlı-smoke--sağlık-kontrol-hattı-03-smoke-verification-pipeline)
6. [Parametrik Çalıştırma ve Raporları Görüntüleme](#6-parametrik-çalıştırma-ve-raporları-görüntüleme)
7. [Jenkins Ana Sayfasında Sekmeler (Views) Oluşturma](#7-jenkins-ana-sayfasında-sekmeler-views-oluşturma)

---

## 1. Ön Hazırlık: İlk Giriş ve Gerekli Eklentiler (Plugins)

### 1.1. Jenkins Yönetici Şifresi
Eğer Jenkins'i yeni başlattıysanız, ilk şifreyi terminalden şu komutla alın:

```bash
docker exec -it jenkins-qa cat /var/jenkins_home/secrets/initialAdminPassword
```

Tarayıcınızda [http://localhost:8080](http://localhost:8080) açıp şifreyi yapıştırın ve **"Install suggested plugins"** seçeneği ile kurulumu tamamlayın.

---

### 1.2. Kurulması Gereken 3 Kritik Eklenti
Raporların ve performans grafiklerinin Jenkins arayüzünde düzgün görünmesi için:

1. Sol menüden **`Manage Jenkins`** ➡️ **`Plugins`** ➡️ **`Available plugins`** sekmesine tıklayın.
2. Arama kutusuna sırasıyla şunları yazıp kutucuklarını işaretleyin:
   * 📦 **`Allure Jenkins Plugin`** *(UI testlerinin Allure raporları için)*
   * 📦 **`HTML Publisher plugin`** *(JMeter interaktif HTML dashboard için)*
   * 📦 **`Performance Plugin`** *(JMeter JTL metrik trend grafikleri için)*
3. **`Install`** butonuna basın ve kurulum bitince Jenkins'i yeniden başlatın.

---

## 2. Global Araç Yapılandırması (Global Tool Configuration)

Pipeline dosyalarımızın ihtiyaç duyduğu Maven ve Allure araçlarını Jenkins'e tanıtalım:

1. **`Manage Jenkins`** ➡️ **`Tools`** sayfasına gidin.
2. **Maven Installations** başlığı altında **`Add Maven`** butonuna tıklayın:
   * **Name:** `Maven_3.9.6`  *(⚠️ Birebir bu isim olmalıdır; Jenkinsfile bu ismi arar)*
   * **Install automatically:** İşaretleyin.
   * **Version:** `3.9.6` seçin.
3. **Allure Commandline Installations** başlığı altında **`Add Allure Commandline`** butonuna tıklayın:
   * **Name:** `Allure`
   * **Install automatically:** İşaretleyin.
   * **Version:** En güncel sürümü (örn: `2.27.0` veya üzeri) seçin.
4. En alttan **`Save` (Kaydet)** butonuna tıklayın.

---

## 3. Item 1: UI Test Otomasyon Hattı (`01-UI-Regression-Pipeline`)

Bu iş; Web UI testlerini Docker Selenium Grid üzerindeki Chrome/Firefox tarayıcılarında paralel koşturur ve Allure raporunu üretir.

```
┌────────────────────────────────────────────────────────────────────────┐
│ ITEM 1: 01-UI-Regression-Pipeline                                      │
│ • Dosya: ci/pipelines/Jenkinsfile.ui                                   │
│ • Tetikleyici: Her 5 dakikada bir Git kontrolü (Poll SCM: H/5 * * * *) │
│ • Çıktı: Allure Test Report & Screenshot Ekleri                        │
└────────────────────────────────────────────────────────────────────────┘
```

### 🖱️ Adım Adım Oluşturma:
1. Jenkins ana sayfasında sol üstteki **`New Item`** (Yeni Öğe) butonuna tıklayın.
2. **Enter an item name:** `01-UI-Regression-Pipeline` yazın.
3. Tip olarak **`Pipeline`** seçin ve **`OK`** butonuna basın.
4. **General & Build Triggers Sekmesi:**
   * **GitHub hook trigger for GITScm polling** veya **Poll SCM** kutusunu işaretleyip `H/5 * * * *` yazın.
5. **Pipeline Sekmesi (Sayfanın En Altı):**
   * **Definition:** `Pipeline script from SCM` seçin.
   * **SCM:** `Git` seçin.
   * **Repository URL:** Projenizin Git repo adresini yazın (Örn: `https://github.com/MuhammetEmirErkut/qa-project-101.git`).
   * **Branches to build:** `*/main` (veya `*/master`).
   * **Script Path:** `Jenkinsfile` yerine ➡️ **`ci/pipelines/Jenkinsfile.ui`** yazın!
6. **`Save`** butonuna tıklayın.

---

## 4. Item 2: JMeter Performans & Yük Test Hattı (`02-Performance-Testing-Pipeline`)

Bu iş; Mock E-Commerce API üzerinde **Smoke**, **Load**, **Stress**, **Spike** veya **Soak** testlerinin tamamını tek çatı altında koşturur ve zengin HTML Dashboard üretir.

```
┌────────────────────────────────────────────────────────────────────────┐
│ ITEM 2: 02-Performance-Testing-Pipeline                                │
│ • Dosya: ci/pipelines/Jenkinsfile.perf                                 │
│ • Parametreler: TEST_TYPE (SMOKE, LOAD, STRESS, SPIKE, SOAK)           │
│ • Tetikleyici: Her Gece 02:00 (Cron: H 2 * * 1-5)                      │
│ • Çıktı: Interaktif JMeter HTML Dashboard & APDEX Skorları             │
└────────────────────────────────────────────────────────────────────────┘
```

### 🖱️ Adım Adım Oluşturma:
1. Jenkins ana sayfasında **`New Item`** butonuna tıklayın.
2. **Enter an item name:** `02-Performance-Testing-Pipeline` yazın.
3. Tip olarak **`Pipeline`** seçin ve **`OK`** butonuna basın.
4. **Pipeline Sekmesi:**
   * **Definition:** `Pipeline script from SCM` seçin.
   * **SCM:** `Git` seçin.
   * **Repository URL:** Git repo adresinizi yazın.
   * **Branches to build:** `*/main` (veya `*/master`).
   * **Script Path:** **`ci/pipelines/Jenkinsfile.perf`** yazın!
5. **`Save`** butonuna tıklayın.

> [!TIP]
> İlk koşumu yaptıktan sonra Jenkins bu dosyadaki parametreleri (`TEST_TYPE`, `HOST`, `PORT`) otomatik olarak algılar ve sol menüdeki **"Build"** butonu kendiliğinden **"Build with Parameters"** butonuna dönüşür! İster hızlı 1 dakikalık `SMOKE`, ister 200 kişilik `STRESS` testini tek tıkla seçip başlatabilirsiniz.

## 5. Item 3: Appium Mobil Web Test Hattı (`03-Mobile-Web-Pipeline`)

Bu iş; Appium Server üzerinden mobil web testlerini (Android Chrome / iOS Safari) otomatik koşturur, mobil cihaz ekran görüntülerini ve Allure raporunu üretir.

```
┌────────────────────────────────────────────────────────────────────────┐
│ ITEM 3: 03-Mobile-Web-Pipeline                                         │
│ • Dosya: ci/pipelines/Jenkinsfile.mobile                               │
│ • Parametreler: PLATFORM, DEVICE_NAME, BROWSER, SUITE_XML              │
│ • Appium Sunucu: http://appium:4723/ (Docker İçi)                      │
│ • Çıktı: Allure Mobile Test Report & Failure Screenshots               │
└────────────────────────────────────────────────────────────────────────┘
```

### 🖱️ Adım Adım Oluşturma:
1. Jenkins ana sayfasında **`New Item`** (Yeni Öğe) butonuna tıklayın.
2. **Enter an item name:** `03-Mobile-Web-Pipeline` yazın.
3. Tip olarak **`Pipeline`** seçin ve **`OK`** butonuna basın.
4. **General & Build Triggers Sekmesi:**
   * **Poll SCM** kutusunu işaretleyip `H/5 * * * *` yazabilirsiniz.
5. **Pipeline Sekmesi (Sayfanın En Altı):**
   * **Definition:** `Pipeline script from SCM` seçin.
   * **SCM:** `Git` seçin.
   * **Repository URL:** Projenizin Git repo adresini yazın.
   * **Branches to build:** `*/main` (veya `*/master`).
   * **Script Path:** ➡️ **`ci/pipelines/Jenkinsfile.mobile`** yazın!
6. **`Save` (Kaydet)** butonuna tıklayın.

---

## 6. Parametrik Çalıştırma ve Raporları Görüntüleme

### 6.1. Parametre Seçerek Test Başlatma (Build with Parameters)
Bir job'ın içerisine girdiğinizde sol menüden **`Build with Parameters`** seçeneğine tıklayın:

* **UI Testi İçin:**
  * `BROWSER` ➡️ `chrome` veya `firefox`
  * `HEADLESS` ➡️ `true` veya `false`
  * `THREAD_COUNT` ➡️ `3` (Paralel oturum sayısı)
* **Performans Testi İçin:**
  * `TEST_TYPE` ➡️ `LOAD` / `STRESS` / `SPIKE` / `SOAK`
  * `HOST` ➡️ `localhost` (veya Docker içinden `mock-api`)
  * `PORT` ➡️ `3001`

**`Build`** butonuna bastığınız anda test seçilen ayarlarla otomatik başlar!

---

### 6.2. Test Raporlarını İnceleme

Test koşumu tamamlandığında:
1. **Allure Report (UI Testleri):**  
   Sol menüde veya son build numarasının (#1, #2) yanında beliren **`Allure Report`** ikonuna tıklayın. Ekran görüntüleri, adım adım loglar ve hata analizleri açılır.
2. **JMeter HTML Dashboard (Performans Testleri):**  
   Sol menüde beliren **`JMeter LOAD Performance Dashboard`** bağlantısına tıklayın. Throughput (RPS), 95th Percentile yanıt süreleri ve APDEX grafikleri doğrudan Jenkins penceresi içinde açılır.

---

## 7. Jenkins Ana Sayfasında Sekmeler (Views) Oluşturma

Tüm job'lar ana sayfada alt alta karışmasın diye sekmelere ayırabilirsiniz:

1. Jenkins ana sayfasındaki **`+` (New View)** sekmesine tıklayın.
2. **View name:** `Performance Tests` yazın ➡️ **`List View`** seçin ➡️ **`OK`**.
3. **Job Filters:** `02-Performance-Testing-Pipeline` kutucuğunu işaretleyin.
4. **`OK`** butonuna basın.

Böylece ana sayfanızda **`All`**, **`UI Tests`** ve **`Performance Tests`** şeklinde temiz kurumsal bir kontrol paneli (Dashboard) elde edersiniz.
