# 📊 Kurumsal Gözlemlenebilirlik (Observability), APM ve Canlı Grafana Dashboard Rehberi

> **Hedef:** DevOps Sunucu İzleme (Prometheus), Uygulama/API İzleme (APM), Canlı JMeter Performans Takibi (InfluxDB + Grafana) ve QA Test Başarı Trendlerini Tek Ekranda Yönetmek  
> **Amaç:** Sektörde kullanılan modern izleme mimarilerini teorisiyle, tanımlarıyla, çalışma mantığıyla ve adım adım Docker kurulumlarıyla sıfırdan öğrenip hayata geçirmek.

---

## 📑 İçindekiler

1. [Büyük Resim: Observability (Gözlemlenebilirlik) Nedir?](#1-büyük-resim-observability-gözlemlenebilirlik-nedir)
   - [1.1. Gözlemlenebilirliğin 3 Temel Sütunu (Metrics, Logs, Traces)](#11-gözlemlenebilirliğin-3-temel-sütunu-metrics-logs-traces)
   - [1.2. QA ve SDET İçin Neden Hayatidir?](#12-qa-ve-sdet-için-neden-hayatidir)
2. [1. Modül: DevOps & Sunucu İzleme (Prometheus + Node Exporter + Grafana)](#2-1-modül-devops--sunucu-izleme-prometheus--node-exporter--grafana)
   - [2.1. Temel Kavramlar (TSDB, Pull/Scraping Modeli, Exporter, PromQL)](#21-temel-kavramlar-tsdb-pullscraping-modeli-exporter-promql)
   - [2.2. Nasıl Çalışır? (Mimari Akış)](#22-nasıl-çalışır-mimari-akış)
   - [2.3. Kurulum ve Yapılandırma Adımları](#23-kurulum-ve-yapılandırma-adımları)
3. [2. Modül: Uygulama ve API İzleme (APM - Application Performance Monitoring)](#3-2-modül-uygulama-ve-api-izleme-apm---application-performance-monitoring)
   - [3.1. APM Nedir ve Neyi Ölçer? (HTTP 500, Latency, DB Queries)](#31-apm-nedir-ve-neyi-ölçer-http-500-latency-db-queries)
   - [3.2. Golden Signals (4 Altın Sinyal)](#32-golden-signals-4-altın-sinyal)
   - [3.3. Alarm ve Bildirim Mekanizmaları (Alerting)](#33-alarm-ve-bildirim-mekanizmaları-alerting)
4. [3. Modül: Canlı Performans Testi İzleme (JMeter + InfluxDB + Grafana)](#4-3-modül-canlı-performans-testi-izleme-jmeter--influxdb--grafana)
   - [4.1. Neden Canlı İzlemeye İhtiyaç Duyarız? (Geleneksel Rapor vs Canlı Yayın)](#41-neden-canlı-izlemeye-ihtiyaç-duyarız-geleneksel-rapor-vs-canlı-yayın)
   - [4.2. JMeter Backend Listener Mimarisi](#42-jmeter-backend-listener-mimarisi)
   - [4.3. Adım Adım Kurulum (InfluxDB + JMeter JMX Yapılandırması)](#43-adım-adım-kurulum-influxdb--jmeter-jmx-yapılandırması)
   - [4.4. Grafana'da Hazır JMeter Dashboard'unu Yükleme (ID: 5496)](#44-grafanada-hazır-jmeter-dashboardunu-yükleme-id-5496)
5. [4. Modül: QA Otomasyon Trendleri ve Yönetim Dashboard'ları](#5-4-modül-qa-otomasyon-trendleri-ve-yönetim-dashboardları)
   - [5.1. Haftalık Test Başarı Oranı (% Pass Rate) Takibi](#51-haftalık-test-başarı-oranı--pass-rate-takibi)
   - [5.2. Flaky (Kararsız) Testleri ve Süre Darboğazlarını Yakalama](#52-flaky-kararsız-testleri-ve-süre-darboğazlarını-yakalama)
6. [5. Modül: Tek Komutla Bütünleşik Docker Compose Ortamı](#6-5-modül-tek-komutla-bütünleşik-docker-compose-ortamı)
7. [Sözlük ve Altın Kurallar](#7-sözlük-ve-altın-kurallar)

---

## 1. Büyük Resim: Observability (Gözlemlenebilirlik) Nedir?

Geleneksel test yaklaşımında sistem bir **"Kara Kutu" (Black-Box)** gibidir: Sisteme istek atarsınız, yanıt 5 saniye sonra gelirse *"Sistem yavaşladı"* dersiniz ama **içeride neyin tıkandığını bilemezsiniz**.

**Observability (Gözlemlenebilirlik)**; sistemin dışarıya yaydığı sinyallere bakarak **içeride tam olarak ne olduğunu, hangi veritabanı sorgusunun kilitlendiğini, hangi sunucunun RAM'inin bittiğini anında anlayabilme yeteneğidir.**

```
┌─────────────────────────────────────────────────────────────────────────┐
│                        OBSERVABILITY (3 SÜTUN)                          │
├───────────────────┬─────────────────────────┬───────────────────────────┤
│    1. METRICS     │        2. LOGS          │        3. TRACES          │
│  "Bir sorun var!" │ "Neden sorun çıktı?"    │ "Sorun tam nerede oldu?"  │
│  (CPU %95, 500/s) │ (NullPointerException)  │ (Ödeme Servisi -> DB 4s)  │
└───────────────────┴─────────────────────────┴───────────────────────────┘
```

### 1.1. Gözlemlenebilirliğin 3 Temel Sütunu

1. **Metrics (Sayısal Metrikler):** Zamana bağlı sayısal verilerdir.  
   *Örnek:* CPU kullanımı `%88`, Saniyedeki İstek (RPS) `450`, Boş RAM `512 MB`. (Araç: **Prometheus, InfluxDB**)
2. **Logs (Olay Günlükleri):** Bir olay anında üretilen metin kayıtlarıdır.  
   *Örnek:* `[2026-09-15 09:30:12] ERROR OrderService - Payment gateway timeout for Order #9182`. (Araç: **Loki, ElasticSearch**)
3. **Traces (İz Sürme):** Bir kullanıcının tıkladığı andan itibaren isteğin mikroservisler arasındaki yolculuğunun haritasıdır.  
   *Örnek:* İstek Toplam 2.5 sn sürdü ➡️ API Gateway (50ms) ➡️ Sepet Servisi (100ms) ➡️ Ödeme Servisi (2350ms - Darboğaz!). (Araç: **OpenTelemetry, Jaeger**)

### 1.2. QA ve SDET İçin Neden Hayatidir?
Bir yük testi yaptığınızda eğer yanıt süreleri 200ms'den 4000ms'ye fırladıysa;
* Sorun test otomasyon kodunda mı?
* Backend sunucusunun CPU'su mu yetmedi?
* Yoksa veritabanında indeks olmadığı için disk I/O mu tıkandı?

Gözlemlenebilirlik olmadan bu sorulara cevap veremezsiniz.

---

## 2. 1. Modül: DevOps & Sunucu İzleme (Prometheus + Node Exporter + Grafana)

Sunucuların donanımsal ve işletim sistemi seviyesindeki sağlığını 7/24 izlemek için dünya standardı mimaridir.

```
┌──────────────────────┐         ┌──────────────────────┐         ┌──────────────────────┐
│    NODE EXPORTER     │         │      PROMETHEUS      │         │       GRAFANA        │
│   (Hedef Sunucu)     │◀────────│    (Metrik Havuzu)   │◀────────│   (Görsel Dashboard) │
│ • CPU, RAM, Disk, Ağ │  Pull   │ • Zaman Serisi (TSDB)│ PromQL  │ • TV Panelleri       │
│ • Port: 9100 /metrics│ (15 sn) │ • Port: 9090         │ Sorgusu │ • Port: 3000         │
└──────────────────────┘         └──────────────────────┘         └──────────────────────┘
```

### 2.1. Temel Kavramlar
* **Prometheus:** Zaman serisi veritabanıdır (**Time Series Database - TSDB**). Verileri `zaman + metrik_adı + etiketler` şeklinde saklar.
* **Pull (Scraping) Modeli:** Prometheus, sunucuların kendisine veri göndermesini beklemez; kendisi gidip her 15 saniyede bir hedef sunuculardaki `/metrics` endpoint'ini çeker (**Scrape eder**).
* **Node Exporter:** Linux/Mac sunucunun işletim sistemi metriklerini (CPU çekirdekleri, bellek doluluğu, disk okuma/yazma hızı) toplayıp Prometheus'un anlayacağı formatta dışarı sunan hafif bir ajandır.
* **PromQL (Prometheus Query Language):** Prometheus'tan veri sorgulama dilidir.  
  *Örnek PromQL:* `100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)` *(Son 5 dakikadaki ortalama CPU kullanım yüzdesi).*

---

## 3. 2. Modül: Uygulama ve API İzleme (APM)

APM (Application Performance Monitoring), sadece sunucu donanımını değil, **yazdığınız Java/Node.js/Go kodunun ve API'lerinizin iç sağlığını** izler.

### 3.1. Golden Signals (4 Altın Sinyal)
Google SRE (Site Reliability Engineering) kitabına göre her sistemde izlenmesi gereken 4 metrik:

```
┌────────────────────────────────────────────────────────────────────────┐
│                        THE 4 GOLDEN SIGNALS                            │
├─────────────────┬──────────────────────────────────────────────────────┤
│ 1. Latency      │ İsteklerin tamamlanma süresi (P95 < 500ms mi?)       │
│ 2. Traffic      │ Sisteme gelen yük / talep miktarı (RPS - Req/sec)    │
│ 3. Errors       │ Başarısız istek oranı (HTTP 500, 502, 504 %'si)     │
│ 4. Saturation   │ Sistemin doluluk oranı (Belleğin % kaçı dolu?)       │
└─────────────────┴──────────────────────────────────────────────────────┘
```

### 3.2. Alarm ve Bildirim Mekanizmaları (Alerting)
Grafana ve Prometheus Alertmanager sayesinde kritik eşikler aşıldığında ekibe otomatik bildirim gönderilir:
* **Kural:** Eğer `HTTP 500 Error Rate > %2` ise (Son 2 dakika boyunca)
* **Aksiyon:** Anında QA ve DevOps Slack kanalına `@channel` etiketiyle alarm gönder:  
  `🚨 KRİTİK: Ödeme API'sinde hata oranı %5'e ulaştı! Dashboard: http://grafana:3000/d/api-health`

---

## 4. 3. Modül: Canlı Performans Testi İzleme (JMeter + InfluxDB + Grafana)

Bu yapı, kurumsal şirketlerde **yük testi yaparken en çok kullanılan ve en havalı mimaridir.**

### 4.1. Neden Canlı İzlemeye İhtiyaç Duyarız?

| Özellik | Standart JMeter HTML Raporu | Canlı Grafana + InfluxDB |
|---|---|---|
| **Veri Görünürlüğü** | Test **tamamen bittikten sonra** rapor üretilir. | Test koşarken **saniye saniye canlı** akar. |
| **Erken Müdahale** | 2 saatlik testin 5. dakikasında sistem çökerse 2 saat boşuna beklersiniz. | Çöküşü 10. saniyede canlı görüp testi anında durdurabilirsiniz. |
| **TV / Dashboard** | Statik HTML dosyasıdır. | Şirket duvarındaki dev TV ekranına yansıtılabilir. |

---

### 4.2. JMeter Backend Listener Mimarisi

JMeter'ın içinde yer alan **Backend Listener**, her bir HTTP isteğinin sonucunu (yanıt süresi, durum kodu, thread sayısı) test biter bitmez değil, **oluştuğu milisaniyede InfluxDB veritabanına fırlatır (Push eder).**

```
┌──────────────────────────────┐
│        APACHE JMETER         │
│  (500 Virtual Users Koşuyor) │
│                              │
│   [ Backend Listener ]       │
└──────────────┬───────────────┘
               │ Her 1 saniyede metrikleri Push eder (UDP/HTTP)
               ▼
┌──────────────────────────────┐
│          INFLUXDB            │
│  (Zaman Serisi Veritabanı)   │
│  • Measurement: jmeter       │
│  • Port: 8086                │
└──────────────┬───────────────┘
               │ Canlı Sorgu (Flux / InfluxQL)
               ▼
┌──────────────────────────────┐
│       GRAFANA DASHBOARD      │
│  (Canlı Akan TV Ekranı)      │
│  • Aktif Sanal Kullanıcılar  │
│  • Canlı Throughput (RPS)    │
│  • 95th Percentile Grafiği   │
└──────────────────────────────┘
```

---

### 4.3. Adım Adım Canlı JMeter İzleme Kurulumu

#### Adım 1: JMeter Test Planına Backend Listener Ekleme (GUI)
1. JMeter'ı açın (`jmeter`).
2. Test Planınızdaki **`Thread Group`** öğesine sağ tıklayın:
   * **`Add`** ➡️ **`Listener`** ➡️ **`Backend Listener`** seçin.
3. Sağ taraftaki ayarlardan:
   * **Backend Listener implementation:**  
     `org.apache.jmeter.visualizers.backend.influxdb.InfluxdbBackendListenerClient` seçin.
4. Tablodaki parametreleri doldurun:

| Parametre | Değeri | Açıklama |
|---|---|---|
| `influxdbMetricsSender` | `org.apache.jmeter.visualizers.backend.influxdb.HttpMetricsSender` | HTTP üzerinden gönderim |
| `influxdbUrl` | `http://localhost:8086/write?db=jmeter` | InfluxDB adresi ve veritabanı adı |
| `application` | `hepsiburada-mock-api` | Proje / Uygulama etiket adı |
| `measurement` | `jmeter` | Tablo / Measurement adı |
| `summaryOnly` | `false` | Her sampler için ayrı metrik toplansın |
| `percentiles` | `90;95;99` | Hesaplanacak yüzdelik dilimler |

---

### 4.4. Grafana'da Hazır JMeter Dashboard'unu Yükleme (ID: 5496)

Sıfırdan onlarca grafik çizmekle uğraşmazsınız; dünyada standart olan hazır JMeter Dashboard şablonunu tek tıkla içe aktarırsınız:

1. Grafana'yı açın ([http://localhost:3000](http://localhost:3000) - Varsayılan Kullanıcı: `admin` / `admin`).
2. **Data Sources** ➡️ **Add Data Source** ➡️ **InfluxDB** seçin:
   * **URL:** `http://influxdb:8086`
   * **Database:** `jmeter`
   * **Save & Test** butonuna basın (Yeşil onay görmelisiniz).
3. Sol menüden **Dashboards** ➡️ **New** ➡️ **Import** seçeneğine tıklayın.
4. **Find and import dashboards...** kutusuna **`5496`** yazıp **`Load`** butonuna basın *(Apache JMeter Official Dashboard)*.
5. InfluxDB veri kaynağınızı seçip **`Import`** deyin.

🎉 **Sonuç:** Testi başlattığınız anda Grafana ekranında sanal kullanıcıların girişi, RPS artışı ve yanıt süreleri **canlı olarak akmaya başlar!**

---

## 5. 4. Modül: QA Otomasyon Trendleri ve Yönetim Dashboard'ları

Yönetim ve liderlik ekipleri teknik kod detaylarıyla ilgilenmez; **kalite trendlerini** görmek ister:

```
┌────────────────────────────────────────────────────────────────────────┐
│                     QA EXECUTIVE QUALITY DASHBOARD                     │
├──────────────────────────┬──────────────────────────┬──────────────────┤
│ Weekly Test Pass Rate    │ Total Automated Tests    │ Flaky Tests      │
│         98.4% 🟢         │          142 🧪          │       2 ⚠️       │
├──────────────────────────┴──────────────────────────┴──────────────────┤
│ 📈 Test Execution Duration Trend (Son 30 Günün Koşum Süresi)          │
│ 4.5m ───┐      ┌───┐                                                   │
│ 3.0m    └──/\──┘   └─── (Hedef: < 5 Dakika)                            │
├────────────────────────────────────────────────────────────────────────┤
│ 🗺️ Module Health Heatmap:                                              │
│ [Login: %100 🟢]  [Cart: %100 🟢]  [Search: %99 🟢]  [Payment: %92 🟡] │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 6. 5. Modül: Tek Komutla Bütünleşik Docker Compose Ortamı

Tüm bu izleme ve görselleştirme araçlarını bilgisayarınıza tek tek kurmak yerine, **tek bir `docker-compose` dosyasıyla** 10 saniyede ayağa kaldırabilirsiniz.

### 📄 `ci/docker-compose.monitoring.yml`

```yaml
services:
  # =============================================================================
  # 1. Prometheus (Metrics TSDB Engine)
  # =============================================================================
  prometheus:
    image: prom/prometheus:v2.51.0
    container_name: monitoring-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    networks:
      - qa-network

  # =============================================================================
  # 2. Node Exporter (Host/OS Hardware Metrics)
  # =============================================================================
  node-exporter:
    image: prom/node-exporter:v1.7.0
    container_name: monitoring-node-exporter
    ports:
      - "9100:9100"
    networks:
      - qa-network

  # =============================================================================
  # 3. InfluxDB (JMeter Real-Time Performance DB)
  # =============================================================================
  influxdb:
    image: influxdb:1.8-alpine
    container_name: monitoring-influxdb
    ports:
      - "8086:8086"
    environment:
      - INFLUXDB_DB=jmeter
    volumes:
      - influxdb_data:/var/lib/influxdb
    networks:
      - qa-network

  # =============================================================================
  # 4. Grafana (Unified Visualization Dashboard)
  # =============================================================================
  grafana:
    image: grafana/grafana:10.4.1
    container_name: monitoring-grafana
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_USER=admin
      - GF_SECURITY_ADMIN_PASSWORD=admin
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana_data:/var/lib/grafana
    depends_on:
      - prometheus
      - influxdb
    networks:
      - qa-network

networks:
  qa-network:
    external: true

volumes:
  influxdb_data:
  grafana_data:
```

### 📄 `ci/prometheus.yml` (Prometheus Yapılandırması)

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

---

## 7. Sözlük ve Altın Kurallar

| Terim | Tanımı |
|---|---|
| **TSDB (Time Series Database)** | Zaman damgalı (timestamped) metrik verilerini yüksek hızda depolamak için optimize edilmiş veritabanları (Prometheus, InfluxDB). |
| **Scraping (Pull)** | Sunucunun gidip hedef servislerin metrik uç noktalarından veriyi düzenli aralıklarla çekmesi. |
| **Pushing** | JMeter gibi test araçlarının metrik üretir üretmez veritabanına göndermesi. |
| **PromQL** | Prometheus üzerinde matematiksel ve istatistiksel sorgular çalıştırma dili. |
| **Backend Listener** | JMeter'ın test sonuçlarını anlık olarak harici sistemlere (InfluxDB, Kafka, Graphite) akıtmasını sağlayan özel eklentisi. |

---

### 💡 Altın Tavsiyeler

1. **JMeter'da `summaryOnly=false` Yaparken Dikkat:** Eğer 100.000 sanal kullanıcı koşuyorsanız, her isteği tek tek InfluxDB'ye yazmak InfluxDB'yi yorabilir. Çok devasa yük testlerinde `summaryOnly=true` yaparak 5 saniyelik özetler göndermek en iyi pratiktir.
2. **Prometheus ile Altyapıyı, JMeter ile İstemciyi İzleyin:** Yük testi yaparken bir ekranda **Grafana JMeter Dashboard** (Kullanıcı tarafındaki yanıt süresi), yanındaki ekranda **Grafana Node Exporter** (Sunucunun CPU/RAM durumu) açık olmalıdır. Böylece yavaşlamanın tam anında CPU'nun fırlayıp fırlamadığını saniyesi saniyesine korele edebilirsiniz!
