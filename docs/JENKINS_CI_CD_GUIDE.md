# 🚀 Jenkins CI/CD & Test Automation Guide (Deep-Dive & Implementation)

Bu kapsamlı rehberde; **Jenkins**'in ne olduğunu, test otomasyonundaki (Black-Box / E2E) tüm işlevlerini, mimarisini ve **bu projeye (`qa-project-101`)** adım adım nasıl entegre edileceğini en ince ayrıntısına kadar bulacaksınız.

---

## 📑 İçindekiler
1. [Jenkins Nedir ve Test Otomasyonundaki Rolü](#1-jenkins-nedir-ve-test-otomasyonundaki-rolü)
2. [Temel Jenkins Mimarisi & Kavramları](#2-temel-jenkins-mimarisi--kavramları)
3. [Neden Docker ile Jenkins ve Selenium Grid?](#3-neden-docker-ile-jenkins-ve-selenium-grid)
4. [Projeye Özel Jenkinsfile Mimarisi](#4-projeye-özel-jenkinsfile-mimarisi)
5. [Docker Compose ile Bütünleşik QA Ortamı Kurulumu](#5-docker-compose-ile-bütünleşik-qa-ortamı-kurulumu)
6. [GitHub ile Jenkins Entegrasyonu (Adım Adım)](#6-github-ile-jenkins-entegrasyonu-adım-adım)
7. [Allure Report Entegrasyonu](#7-allure-report-entegrasyonu)
8. [Parametrik Koşular & Zamanlanmış Görevler (Cron)](#8-parametrik-koşular--zamanlanmış-görevler-cron)
9. [Sık Karşılaşılan Sorunlar ve Çözümleri (Troubleshooting)](#9-sık-karşılaşılan-sorunlar-ve-çözümleri-troubleshooting)

---

## 🧠 1. Jenkins Nedir ve Test Otomasyonundaki Rolü?

**Jenkins**, açık kaynaklı bir **Otomasyon & Sürekli Entegrasyon (CI - Continuous Integration)** sunucusudur. 

Yazılım geliştirme ve QA süreçlerinde tekrarlanan işleri (test çalıştırma, raporlama, bildirim gönderme, ortam hazırlama) otomatikleştiren bir orkestratördür.

```
+-------------------------------------------------------------------------+
|                              JENKINS PIPELINE                           |
|                                                                         |
|  +---------------+     +---------------+     +-----------------------+  |
|  |  1. Checkout  | --> |  2. Run Tests | --> |  3. Generate Reports  |  |
|  |  (From Git)   |     |  (Maven/Grid) |     |  (Allure / TestNG)    |  |
|  +---------------+     +---------------+     +-----------------------+  |
|                                                          |              |
|                                                          v              |
|                                               +----------------------+  |
|                                               |  4. Notify & Alert   |  |
|                                               |  (Slack / Email)     |  |
|                                               +----------------------+  |
+-------------------------------------------------------------------------+
```

### Black-Box Test Otomasyonunda Jenkins Ne İşe Yarar?
1. **Zamanlanmış Gece Koşuları (Nightly Regression):** Siz uyurken her gece 02:00'de tüm regresyon testlerini çalıştırır, sabah Allure raporunu hazırlar.
2. **Canlı Ortam Sağlık Kontrolleri (Synthetic Monitoring):** Her saat başı Hepsiburada'da Arama -> Sepete Ekleme adımlarını test edip olası bir kesintide QA ekibine anında Slack/Mail alarmı yollar.
3. **Paralel & Dağıtık Çalıştırma:** Testleri kendi bilgisayarınızı yormadan Docker/Selenium Grid üzerindeki onlarca sanal tarayıcıya dağıtır.
4. **Tek Tıkla Parametrik Koşu (On-Demand):** İstediğiniz browser (`chrome`/`firefox`), ortam veya thread sayısını Jenkins web arayüzünden seçip tek tıkla test başlatabilirsiniz.

---

## 🏛️ 2. Temel Jenkins Mimarisi & Kavramları

* **Controller (Master):** Jenkins'in beynidir. Web arayüzünü yönetir, zamanlamaları tutar ve işleri (jobs) kuyruğa alır.
* **Agent (Node / Worker):** İşlerin (komutların/testlerin) fiilen çalıştığı sunucu veya Docker konteyneridir.
* **Job / Pipeline:** Çalıştırılacak görevler zinciridir.
* **Declarative Pipeline:** Jenkins adımlarını okunabilir ve kod tabanlı (`Jenkinsfile`) olarak tanımlama standardıdır.
* **Artifact:** Test koşusu sonunda üretilen dosyalar (Allure sonuçları, ekran görüntüleri, log dosyaları).

---

## 🐳 3. Neden Docker ile Jenkins ve Selenium Grid?

Kendi bilgisayarınıza Java, Maven, Jenkins, Chrome, ChromeDriver, Allure CLI kurmak sürüm çakışmalarına (Dependency Hell) yol açar. 

Docker sayesinde:
1. **İzolasyon:** Jenkins kendi konteynerinde, Chrome tarayıcıları Selenium Grid konteynerinde çalışır.
2. **Tek Komutla Kurulum:** `docker compose up -d` komutuyla 10 saniyede tüm QA altyapınız ayağa kalkar.
3. **Taşınabilirlik:** Kendi Mac'inizde çalışan Docker dosyası şirketin Linux sunucusunda veya AWS'te birebir aynı şekilde çalışır.

---

## 📝 4. Projeye Özel Jenkinsfile Mimarisi

Projenizin ana dizinine eklenecek `Jenkinsfile`, bu projenin Maven ve Allure gereksinimlerine göre optimize edilmiştir:

```groovy
// Jenkins Declarative Pipeline Definition for QA Automation
pipeline {
    // Execute on any available agent
    agent any

    // Define configurable build parameters
    parameters {
        choice(name: 'BROWSER', choices: ['chrome', 'firefox'], description: 'Target browser for execution')
        choice(name: 'HEADLESS', choices: ['true', 'false'], description: 'Run browser in headless mode')
        string(name: 'THREAD_COUNT', defaultValue: '3', description: 'Number of parallel execution threads')
        string(name: 'SUITE_XML', defaultValue: 'testng.xml', description: 'TestNG Suite XML file to run')
    }

    // Environment variables used throughout the pipeline
    environment {
        // Points to Selenium Grid Hub container in docker network
        SELENIUM_REMOTE_URL = 'http://selenium-hub:4444'
    }

    // Sequential build stages
    stages {
        // Stage 1: Checkout test automation source code from repository
        stage('Checkout Source') {
            steps {
                echo 'Checking out latest test automation code from Git repository...'
                checkout scm
            }
        }

        // Stage 2: Environment verification and dependency check
        stage('Environment Check') {
            steps {
                echo 'Verifying Java and Maven installation...'
                sh 'java -version'
                sh 'mvn -version'
            }
        }

        // Stage 3: Execute TestNG automated tests via Maven
        stage('Execute Automated Tests') {
            steps {
                echo "Starting test suite execution with Browser=${params.BROWSER}, Headless=${params.HEADLESS}, Threads=${params.THREAD_COUNT}"
                
                // Run Maven test with dynamic properties overriding config.properties
                sh """
                    mvn clean test \
                        -Dbrowser=${params.BROWSER} \
                        -Dheadless=${params.HEADLESS} \
                        -Dremote=true \
                        -DgridUrl=${SELENIUM_REMOTE_URL} \
                        -Dsurefire.suiteXmlFiles=${params.SUITE_XML}
                """
            }
        }
    }

    // Post-execution actions (Runs regardless of build status)
    post {
        always {
            echo 'Archiving Allure test results and generating report...'
            
            // Generate Allure Report from target/allure-results
            allure([
                includeProperties: false,
                jdk: '',
                properties: [],
                reportBuildPolicy: 'ALWAYS',
                results: [[path: 'target/allure-results']]
            ])
            
            // Archive standard surefire test reports
            junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
        }
        success {
            echo 'All automated tests passed successfully! 🚀'
        }
        failure {
            echo 'Test execution failed! Check Allure Report and screenshots for failure root cause. ❌'
        }
    }
}
```

---

## 🛠️ 5. Docker Compose ile Bütünleşik QA Ortamı Kurulumu

Proje ana dizinine eklenecek `docker-compose.yml` dosyası; **Jenkins**, **Selenium Grid Hub**, **Chrome Node** ve **Firefox Node** konteynerlerini tek bir ağ altında bağlar:

```yaml
version: '3.8'

networks:
  qa-network:
    driver: bridge

services:
  # ==========================================
  # 1. JENKINS CI/CD CONTROLLER
  # ==========================================
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    container_name: jenkins-qa
    user: root
    privileged: true
    ports:
      - "8080:8080" # Jenkins Web UI
      - "50000:50000" # Jenkins Agent Port
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock # Allows Jenkins to communicate with host Docker daemon
    networks:
      - qa-network

  # ==========================================
  # 2. SELENIUM GRID HUB (Router)
  # ==========================================
  selenium-hub:
    image: selenium/hub:4.21.0
    container_name: selenium-hub
    ports:
      - "4444:4444" # Selenium Grid Console & Webdriver endpoint
    networks:
      - qa-network

  # ==========================================
  # 3. CHROME BROWSER NODE (Worker)
  # ==========================================
  chrome-node:
    image: selenium/node-chrome:4.21.0
    container_name: selenium-chrome-node
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_SESSIONS=3 # Parallel execution capacity
      - SE_NODE_SESSION_TIMEOUT=300
    shm_size: 2gb # Shared memory for smooth Chrome execution
    networks:
      - qa-network

  # ==========================================
  # 4. FIREFOX BROWSER NODE (Worker)
  # ==========================================
  firefox-node:
    image: selenium/node-firefox:4.21.0
    container_name: selenium-firefox-node
    depends_on:
      - selenium-hub
    environment:
      - SE_EVENT_BUS_HOST=selenium-hub
      - SE_EVENT_BUS_PUBLISH_PORT=4442
      - SE_EVENT_BUS_SUBSCRIBE_PORT=4443
      - SE_NODE_MAX_SESSIONS=2
    shm_size: 2gb
    networks:
      - qa-network

volumes:
  jenkins_home:
```

### Başlatma Komutu:
```bash
# Start all QA infrastructure services in background
docker compose -f docker-compose.yml up -d
```
* **Jenkins UI:** `http://localhost:8080`
* **Selenium Grid UI:** `http://localhost:4444/ui`

---

## 🔗 6. GitHub ile Jenkins Entegrasyonu (Adım Adım)

### Adım 6.1: GitHub Personal Access Token Alma
1. GitHub hesabınızda **Settings** $\rightarrow$ **Developer settings** $\rightarrow$ **Personal access tokens** $\rightarrow$ **Tokens (classic)** yoluna gidin.
2. **Generate new token (classic)** seçin.
3. Yetkilerde `repo` ve `admin:repo_hook` kutularını işaretleyin ve token'ı kopyalayın.

### Adım 6.2: Jenkins'e GitHub Credential Ekleme
1. `http://localhost:8080` $\rightarrow$ **Manage Jenkins** $\rightarrow$ **Credentials** $\rightarrow$ **Global** $\rightarrow$ **Add Credentials**.
2. **Kind:** `Username with password` seçin.
3. **Username:** GitHub kullanıcı adınız.
4. **Password:** Yukarıda aldığınız Personal Access Token.
5. **ID:** `github-credentials` yazıp kaydedin.

### Adım 6.3: Pipeline Job Oluşturma
1. Jenkins ana sayfasında **New Item** butonuna tıklayın.
2. İsim olarak `hepsiburada-qa-regression` yazın ve **Pipeline** türünü seçin.
3. **Pipeline** sekmesinde:
   * **Definition:** `Pipeline script from SCM`
   * **SCM:** `Git`
   * **Repository URL:** `https://github.com/KULLANICI_ADI/qa-project-101.git`
   * **Credentials:** `github-credentials`
   * **Branch Specifier:** `*/main` veya `*/master`
   * **Script Path:** `Jenkinsfile`
4. **Save** butonuna basarak kaydedin.

### Adım 6.4: Otomatik Tetikleme Seçenekleri

#### Seçenek A: Poll SCM (Dışarıya Port Açmadan - Tavsiye Edilen)
* Job ayarlarında **Build Triggers** $\rightarrow$ **Poll SCM** seçin.
* **Schedule:** `H/10 * * * *` (Her 10 dakikada bir GitHub'ı kontrol eder; yeni commit varsa testi otomatik başlatır).

#### Seçenek B: GitHub Webhook (Ngrok ile Anında Tetikleme)
1. Terminalden Ngrok tüneli açın:
   ```bash
   ngrok http 8080
   ```
2. GitHub reponuzda **Settings** $\rightarrow$ **Webhooks** $\rightarrow$ **Add webhook**:
   * **Payload URL:** `https://SIZIN_NGROK_ADRESINIZ.ngrok-free.app/github-webhook/`
   * **Content type:** `application/json`
3. Artık her `git push` anında Jenkins otomatik başlayacaktır.

---

## 📊 7. Allure Report Entegrasyonu

1. Jenkins $\rightarrow$ **Manage Jenkins** $\rightarrow$ **Plugins** $\rightarrow$ **Available plugins**.
2. **Allure** eklentisini aratıp kurun (`Allure Jenkins Plugin`).
3. **Manage Jenkins** $\rightarrow$ **Tools** $\rightarrow$ **Allure Commandline**:
   * **Name:** `Allure_CLI`
   * **Install automatically:** İşaretleyin $\rightarrow$ Son sürümü seçin.
4. `Jenkinsfile` içerisindeki `allure(...)` bloğu her koşu sonunda otomatik olarak Allure dashboard'unu oluşturup Jenkins sol menüsüne **Allure Report** ikonunu ekleyecektir.

---

## ⏰ 8. Parametrik Koşular & Zamanlanmış Görevler (Cron)

### 8.1. Zamanlanmış Gece Koşuları (Cron)
`Jenkinsfile` içine `triggers` bloğu eklenerek haftalık veya günlük otomatik koşular planlanabilir:

```groovy
pipeline {
    agent any
    
    // Automatically trigger test suite execution on schedule
    triggers {
        // Run every weekday (Monday-Friday) at 02:00 AM (UTC)
        cron('0 2 * * 1-5')
    }
    
    // ... stages ...
}
```

### 8.2. Jenkins Web UI Üzerinden Parametre ile Çalıştırma
Job oluşturulduktan sonra sol menüde **Build with Parameters** seçeneği belirir:
* **BROWSER:** `chrome` / `firefox`
* **HEADLESS:** `true` / `false`
* **THREAD_COUNT:** `3` / `5`
* **SUITE_XML:** `testng.xml` (veya `smoke-tests.xml`)

---

## 🔧 9. Sık Karşılaşılan Sorunlar ve Çözümleri (Troubleshooting)

### 1. `WebDriverException: SessionNotCreatedException` (Grid Hatası)
* **Sebep:** Jenkins konteyneri Selenium Hub adresine (`http://selenium-hub:4444`) erişemiyordur.
* **Çözüm:** Her iki servisin de aynı Docker network (`qa-network`) içerisinde olduğunu ve `docker-compose.yml` dosyasındaki isimlerin doğru eşleştiğini kontrol edin.

### 2. `OutOfMemoryError` veya Chrome Crash
* **Sebep:** Docker konteynerinin varsayılan paylaşımlı belleği (`/dev/shm` 64MB) Chrome için yetersiz kalıyordur.
* **Çözüm:** `docker-compose.yml` içerisinde `chrome-node` servisine `shm_size: 2gb` satırının eklendiğinden emin olun.

### 3. Allure Raporunun Boş Gelmesi
* **Sebep:** `target/allure-results` klasörü oluşmamıştır.
* **Çözüm:** `pom.xml` dosyasında `allure-testng` bağımlılığının ve `aspectjweaver` argümanının doğru yapılandırıldığını doğrulayın.

---

---

## 📱 10. Appium Mobil Web CI/CD Entegrasyonu & Docker Yapılandırması

Mobil web testlerimizi (Android Chrome / iOS Safari) CI/CD süreçlerine dahil etmek için Docker Compose ve Jenkins altyapımıza Appium entegrasyonu tamamlanmıştır.

### 10.1. Docker Compose İçerisindeki Appium Servisi
`ci/docker-compose.yml` içerisine `appium-server` servisi eklenmiştir:

```yaml
appium:
  image: appium/appium:latest
  container_name: appium-server
  ports:
    - "4723:4723"   # Appium REST API Endpoint
  environment:
    - APPIUM_ADDITIONAL_ARGS=--allow-cors --relaxed-security
    - RELAXED_SECURITY=true
  extra_hosts:
    - "host.docker.internal:host-gateway"
  networks:
    - qa-network
```

### 10.2. Jenkinsfile.mobile Pipeline Dosyası
`ci/pipelines/Jenkinsfile.mobile` dosyası ile Jenkins üzerinden tek tıkla parametrik mobil testler koşturulabilir:

* **PLATFORM:** `ANDROID` / `IOS`
* **DEVICE_NAME:** `Medium_Phone` / `emulator-5554`
* **BROWSER:** `Chrome` / `Safari`
* **SUITE_XML:** `mobile-smoke-tests.xml` / `mobile-regression-tests.xml`
* **APPIUM_SERVER_URL:** `http://appium:4723/` (Docker içi) veya `http://host.docker.internal:4723/` (Host makine)

### 10.3. Makefile ile Tek Satırda Mobil Test Koşumu
```bash
# Mobil Duman (Smoke) Testleri
make test-mobile-smoke

# Mobil Tam Regresyon Paketi
make test-mobile-regression
```

---

## 🎯 Özet Mimarisi

Bu yapı kurulduğunda projeniz şu yetenekleri kazanır:
1. **Sıfır Yerel Bağımlılık:** Bilgisayarınızda Java/Maven kurulu olmasa bile Docker ile tüm Web ve Mobil testler koşar.
2. **Çoklu Platform:** Masaüstü (Chrome, Firefox) ve Mobil (Android Chrome, iOS Safari) testleri tek CI/CD çatı altında yönetilir.
3. **Paralel & Dağıtık Koşu:** Selenium Grid ve Appium sayesinde testler bağımsız konteyner ve emülatörlere dağıtılır.
4. **Görsel Dashboard:** Her testten sonra Allure raporları, ekran görüntüleri ve geçmiş trendleri tek ekranda toplanır.
5. **Sürekli Güvenlik Ağı (CI):** Kod her değiştiğinde veya zamanı geldiğinde insan müdahalesine gerek kalmadan kalite kontrolü sağlanır.
