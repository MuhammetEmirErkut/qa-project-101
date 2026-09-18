# 🚀 Enterprise QA & SDET Test Automation Platform

Bu proje; **UI Test Otomasyonu**, **Performans & Yük Testleri**, **CI/CD Pipeline Entegrasyonları** ve **Canlı Gözlemlenebilirlik (Grafana + Prometheus + InfluxDB)** bileşenlerini tek bir çatı altında toplayan kurumsal düzeyde bir QA/SDET mühendislik platformudur.

---

## 🏗️ Mimari ve Ekosistem

```text
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                                   SHARED QA-NETWORK                                    │
├────────────────────────────────┬───────────────────────────────┬───────────────────────┤
│       1. CI/CD & AUTOMATION    │       2. TEST TARGETS         │    3. OBSERVABILITY   │
│                                │                               │                       │
│  ┌──────────────────────────┐  │  ┌─────────────────────────┐  │  ┌─────────────────┐  │
│  │   Jenkins CI/CD (:8080)  │──┼─>│ Mock E-Comm API (:3001) │  │  │ Grafana (:3000) │  │
│  └─────────────┬────────────┘  │  └─────────────────────────┘  │  └────────┬────────┘  │
│                │               │                               │           │           │
│        ┌───────┴───────┐       │                               │     ┌─────┴─────┐     │
│        ▼               ▼       │                               │     ▼           ▼     │
│  ┌───────────┐   ┌───────────┐ │                               │ ┌─────────┐ ┌────────┐│
│  │ Selenium  │   │  JMeter   │─┼───────────────────────────────┼─>│InfluxDB│ │Prometh.││
│  │   Grid    │   │  Engine   │ │ (Live Test Metrics Push)      │ │ (:8086) │ │ (:9090)││
│  │  (:4444)  │   └───────────┘ │                               │ └─────────┘ └───▲────┘│
│  └─────┬─────┘                 │                               │                 │     │
│        ▼                       │                               │         ┌───────┴───┐ │
│  ┌───────────┐                 │                               │         │Node Expor.│ │
│  │VNC Viewer │                 │                               │         │  (:9100)  │ │
│  │  (:7900)  │                 │                               │         └───────────┘ │
│  └───────────┘                 │                               │                       │
└────────────────────────────────┴───────────────────────────────┴───────────────────────┘
```

---

## 🛠️ Teknoloji Yığını (Tech Stack)

### 1. UI Test Otomasyonu
- **Dil & Motor:** Java 17+, Selenium WebDriver 4.21.0, RemoteWebDriver
- **Test Runner & Pattern:** TestNG 7.10.2, Page Object Model (POM), Fluent Page Pattern
- **Konfigürasyon:** Aeonbits Owner (Type-safe configuration)
- **Raporlama & Tanılama:** Allure Report, Failure Screenshots, DOM HTML Dump, AI-Ready Diagnostik Raporlama
- **Grid Altyapısı:** Dockerize Selenium Hub, Chrome Node, Firefox Node (Canlı VNC akışlı)

### 2. Performans & Yük Testi
- **Yük Motoru:** Apache JMeter 5.6.3 (Non-GUI Execution)
- **Test Profilleri:** Smoke, Load, Stress, Spike ve Soak test profilleri (`.properties`)
- **İzole Hedef API:** Node.js/Express Tabanlı Mock E-Ticaret REST API (`:3001`)

### 3. Gözlemlenebilirlik (Observability & Monitoring)
- **Canlı Test Metrikleri (TSDB):** InfluxDB 1.8 (JMeter `BackendListener` ile milisaniyelik veri akışı)
- **Sistem & Donanım İzleme:** Prometheus v2.51.0 + Node Exporter v1.7.0 (CPU, RAM, Disk, Ağ)
- **Merkezi Görselleştirme:** Grafana 10.4.1 (Birleşik JMeter + Sistem Performans Dashboard'ları)
- **Mimari Uyumluluk:** Apple Silicon (ARM64) ve x86_64 (AMD64) tam uyumlu.

### 4. CI/CD Otomasyonu
- **Orkestrasyon:** Jenkins LTS (Docker Socket & Dinamik Maven/JMeter Runtime)
- **Pipelines:** 
  - `Jenkinsfile.ui`: Selenium Grid üzerinde paralel UI test koşumu, Allure & JUnit raporlama
  - `Jenkinsfile.perf`: Dinamik profilli JMeter koşumu, InfluxDB metrik iletimi, HTML Dashboard arşivi

---

## 🌐 Servis Portları ve Erişim Noktaları

| Servis | Port | URL | Açıklama |
|---|---|---|---|
| **Jenkins CI/CD** | `8080` | [http://localhost:8080](http://localhost:8080) | Pipeline ve Otomasyon Yönetim Paneli |
| **Grafana Dashboard** | `3000` | [http://localhost:3000](http://localhost:3000) | Canlı Performans & Kalite Panosu (`admin` / `admin`) |
| **Selenium Grid Console** | `4444` | [http://localhost:4444](http://localhost:4444) | Grid Hub ve Node Yönetim Arayüzü |
| **Live VNC Chrome Stream**| `7900` | [http://localhost:7900](http://localhost:7900) | Canlı Tarayıcı Koşum İzleme *(Şifre: `secret`)* |
| **Prometheus TSDB** | `9090` | [http://localhost:9090](http://localhost:9090) | Sunucu & Sistem Metrik Motoru |
| **InfluxDB** | `8086` | `http://localhost:8086` | JMeter Canlı Test Veritabanı (`db=jmeter`) |
| **Node Exporter** | `9100` | [http://localhost:9100](http://localhost:9100) | Host Donanım Metrik Çıktısı |
| **Mock E-Commerce API** | `3001` | [http://localhost:3001](http://localhost:3001) | Test Edilen Hedef REST API |

---

## 🚦 Hızlı Başlangıç (Quick Start)

### 1. Altyapıyı Ayağa Kaldırma

Proje konteynerleri ortak bir Docker ağı (`qa-network`) üzerinde haberleşir:

```bash
cd ci

# 1. Ana Test Altyapısını Başlatın (Jenkins, Selenium Grid, Mock API)
docker compose up -d

# 2. Monitoring Stack'i Başlatın (Prometheus, InfluxDB, Node Exporter, Grafana)
docker compose -f docker-compose.monitoring.yml up -d
```

---

### 2. UI Testlerini Çalıştırma

```bash
# Selenium Grid üzerinde çalıştırma (Varsayılan: remote=true)
mvn clean test

# Yerel tarayıcıda çalıştırma
mvn clean test -Dremote=false -Dheadless=false

# Belirli bir suite XML ile çalıştırma
mvn clean test -Dsurefire.suiteXmlFiles=src/test/resources/suites/smoke-suite.xml
```

#### Allure Raporunu Görüntüleme:
```bash
# Canlı Allure sunucusunu başlatıp tarayıcıda açar
mvn allure:serve
```

---

### 3. JMeter Performans Testlerini Çalıştırma

```bash
# Jenkins Pipeline üzerinden:
# Jenkins UI -> JMeter Performance Test -> Build with Parameters (TEST_TYPE=LOAD, HOST=mock-api)

# Veya Terminalden (Non-GUI):
jmeter -n \
  -t performance-test/jmeter/ecommerce_api_test_plan.jmx \
  -q performance-test/jmeter/profiles/load.properties \
  -Jhost=localhost -Jport=3001 \
  -JinfluxdbUrl=http://localhost:8086/write?db=jmeter \
  -l performance-test/jmeter/results.jtl \
  -e -o performance-test/jmeter/html-report
```

---

### 4. Altyapıyı Durdurma

```bash
cd ci
docker compose down && docker compose -f docker-compose.monitoring.yml down
```

---

## 📊 Grafana JMeter Dashboard Kurulumu

1. [http://localhost:3000](http://localhost:3000) adresine gidin (`admin` / `admin`).
2. **Connections > Data Sources > InfluxDB** ekleyin:
   - **URL:** `http://influxdb:8086`
   - **Database:** `jmeter`
   - **Save & Test**'e basın.
3. **Dashboards > Import** bölümüne **`5496`** *(Official JMeter Dashboard)* yazıp InfluxDB veri kaynağını seçerek yükleyin.
4. Test başladığında grafikler gerçek zamanlı olarak akacaktır!

---

## 📋 Hata Yakalama ve Tanılama (Failure Diagnostics)

Test başarısız olduğunda sistem otomatik olarak şu çıktıları üretir:
1. **Ekran Görüntüleri:** `target/failures/screenshots/`
2. **DOM / HTML Kaynağı:** `target/failures/dom_sources/`
3. **AI-Ready Hata Raporları:** `target/failures/reports/<test_name>_error.txt`
4. **Allure Ekleri:** Ekran görüntüleri, loglar ve DOM dökümü doğrudan Allure raporuna iliştirilir.

---

## 📚 Dokümantasyon ve Rehberler

Detaylı teknik rehberlere [`docs/`](docs/) dizininden erişebilirsiniz:

* 🚀 [Test Otomasyon Mimarisi Rehberi](docs/AUTOMATION_GUIDE.md)
* ⚡ [JMeter Performans, Yük & Spike Test Rehberi](docs/JMETER_PERFORMANCE_TESTING_GUIDE.md)
* 📊 [Observability, APM, Prometheus & Canlı Grafana Rehberi](docs/GRAFANA_PROMETHEUS_OBSERVABILITY_GUIDE.md)
* 🤖 [Jenkins CI/CD Mimarisi Rehberi](docs/JENKINS_CI_CD_GUIDE.md)
* 🛠️ [Jenkins Item / Job Oluşturma Rehberi](docs/JENKINS_ITEM_CREATION_GUIDE.md)
* 🛒 [Sepet ve Ürün Detay (PDP) Testleri](docs/CART_AND_PDP_GUIDE.md)
* 🔍 [Filtreleme ve Sıralama Testleri Rehberi](docs/FILTER_AND_SORT_TESTS_GUIDE.md)
* 🔐 [Login ve Negatif Test Senaryoları Rehberi](docs/LOGIN_AND_NEGATIVE_TESTS_GUIDE.md)
* ❤️ [PDP ve İstek Listesi (Wishlist) Testleri](docs/PDP_AND_WISHLIST_TESTS_GUIDE.md)
* ⚡ [Hızlı Koşum Test Rehberi](docs/QUICK_TESTS_GUIDE.md)
