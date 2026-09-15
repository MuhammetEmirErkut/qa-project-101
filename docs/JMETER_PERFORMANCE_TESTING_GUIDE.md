# 🚀 Kurumsal Performans, Yük ve Spike Testi Geliştirme Rehberi (Apache JMeter)

> **Hedef:** E-Ticaret Mimarileri için Apache JMeter ile Performans, Yük (Load), Zorlama (Stress) ve Ani Yük (Spike) Testleri  
> **Test Edilecek Hedef:** İzole Lokal Mock E-Commerce Backend API (`http://localhost:3001`)  
> **Amaç:** Canlı sistemleri tehlikeye atmadan, sıfırdan bir performans testi senaryosu kurgulamak, metrikleri (Throughput, 95th Percentile, APDEX) analiz etmek ve Jenkins CI/CD hattında otomatik raporlamak.

---

## 🚨 KRİTİK GÜVENLİK VE HUKUKİ UYARI

> [!CAUTION]
> ### ⚠️ Canlı E-Ticaret Sitelerine (Hepsiburada, Trendyol vb.) Neden ASLA Yük/Performans Testi Yapılamaz?
> 
> 1. **Hukuki Yaptırımlar (TCK Madde 243 & 244):**  
>    İzinsiz olarak bir web sitesine yüzlerce/binlerce eşzamanlı istek göndermek, Türk Ceza Kanunu kapsamında **"Bilişim Sistemini Engelleme, Bozma, Verileri Yok Etme veya Değiştirme" (DDoS / Hizmet Dışı Bırakma Suçu)** olarak kabul edilir ve **1 yıldan 5 yıla kadar hapis cezası** ile sonuçlanabilir.
> 
> 2. **WAF & Cloudflare Engellemesi:**  
>    Kurumsal sistemlerde Cloudflare, Akamai, Imperva gibi Web Application Firewall (WAF) sistemleri bulunur. Yapacağınız ilk yük testi denemesinde IP adresiniz kalıcı olarak banlanır ve ISP sağlayıcınıza kötüye kullanım (Abuse) bildirimi yapılır.
> 
> 3. **Veritabanı & Ödeme Sistemleri Kilitleme Riski:**  
>    Canlı ortamda test yapmak, gerçek stok verilerini tüketebilir, ödeme sağlayıcılarının (İyzico, Masterpass, Banka POS) oran limitlerini (rate-limit) doldurarak gerçek müşterilerin alışveriş yapmasını engelleyebilir ve milyonlarca liralık ticari zarara yol açabilir.
> 
> **Çözümümüz:**  
> Projemiz içerisine entegre ettiğimiz **Tam İzole, Gerçekçi Mock E-Ticaret API'sini (`localhost:3001`)** kullanarak tüm performans testlerinizi güvenle, yasal olarak ve sınırsızca koşabilirsiniz.

---

## 📑 İçindekiler

1. [Performans Testi Mimarisi ve Büyük Resim](#1-performans-testi-mimarisi-ve-büyük-resim)
2. [Adım 1: Lokal Mock E-Ticaret API'sini Başlatma](#2-adım-1-lokal-mock-e-ticaret-apisini-başlatma)
3. [Adım 2: Apache JMeter Kurulumu](#3-adım-2-apache-jmeter-kurulumu)
4. [Adım 3: JMeter Test Planı Oluşturma ve API'ye Bağlama](#4-adım-3-jmeter-test-planı-oluşturma-ve-apiye-bağlama)
5. [Adım 4: Test Türleri ve Konfigürasyonları (Load, Stress, Spike, Soak)](#5-adım-4-test-türleri-ve-konfigürasyonları)
6. [Adım 5: CLI (Non-GUI) ile Koşum ve Zengin HTML Dashboard Raporu Üretme](#6-adım-5-cli-non-gui-ile-koşum-ve-zengin-html-dashboard-raporu-üretme)
7. [Adım 6: Performans Metriklerini Okuma ve Yorumlama](#7-adım-6-performans-metriklerini-okuma-ve-yorumlama)
8. [Adım 7: Jenkins CI/CD Pipeline Entegrasyonu](#8-adım-7-jenkins-cicd-pipeline-entegrasyonu)
9. [Proje Dosya Ağacı ve Hazır Şablonlar](#9-proje-dosya-ağacı-ve-hazır-şablonlar)

---

## 1. Performans Testi Mimarisi ve Büyük Resim

Performans testlerinde temel amacımız yazılımın sadece "çalışıp çalışmadığını" değil; **hangi yük altında, ne kadar hızla (latency), ne kadar hatayla (error rate) ve kaç eşzamanlı kullanıcıya kadar stabil çalıştığını** ölçmektir.

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           APACHE JMETER                                 │
│  (Thread Groups: 10 - 500 Virtual Users / Sanal Kullanıcı)              │
│                                                                         │
│   ┌─────────────────┐    ┌─────────────────┐    ┌──────────────────┐    │
│   │ 01. POST Login  │───▶│ 02. GET Products│───▶│ 03. POST AddCart │... │
│   └────────┬────────┘    └─────────────────┘    └──────────────────┘    │
│            │ (Extract JWT Token)                                        │
│            └─────────────────────────────────────────────────────┐      │
└────────────────────────────────────┬─────────────────────────────│──────┘
                                     │ HTTP İstekleri              │ Bearer Auth
                                     ▼                             ▼
┌─────────────────────────────────────────────────────────────────────────┐
│              LOCAL MOCK E-COMMERCE API (localhost:3001)                 │
│                                                                         │
│  • /api/health                      • /api/cart/items (Auth)            │
│  • /api/auth/login                  • /api/checkout   (Auth)            │
│  • /api/products (Search & Filter)  • /api/stress/heavy-computation     │
│  • /api/products/:id                • /api/spike/flash-sale-buy         │
└────────────────────────────────────┬────────────────────────────────────┘
                                     │ Test Sonuçları (.jtl)
                                     ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                 INTERACTIVE HTML DASHBOARD & JENKINS                    │
│  • APDEX Score (0.0 - 1.0)          • 90th, 95th, 99th Percentiles      │
│  • Throughput (RPS)                 • Error % Trend Grafikleri          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Adım 1: Lokal Mock E-Ticaret API'sini Başlatma

Performans testlerini koşturacağımız backend sunucusu `performance-test/mock-api/server.js` dosyasında **sıfır bağımlılıkla (Zero-Dependency Node.js)** hazır olarak yer almaktadır.

### 2.1. Sunucuyu Terminalden Başlatma

Terminalinizi açın ve proje ana dizinindeyken şu komutu çalıştırın:

```bash
node performance-test/mock-api/server.js
```

Başarılı çıktıyı göreceksiniz:
```text
=======================================================
🚀 QA Mock E-Commerce API is running on port 3001
👉 Base URL: http://localhost:3001
👉 Health Check: http://localhost:3001/api/health
👉 Product List: http://localhost:3001/api/products
👉 Login: POST http://localhost:3001/api/auth/login
=======================================================
```

> [!TIP]
> Sunucuyu arka planda çalıştırmak isterseniz:  
> `node performance-test/mock-api/server.js &`

### 2.2. API Endpoint Kataloğu

| Metot | Endpoint | Açıklama | Auth Gerekli mi? |
|---|---|---|:---:|
| `GET` | `/api/health` | Servis sağlık ve bellek durumu | ❌ Hayır |
| `POST` | `/api/auth/login` | Kullanıcı girişi (JWT Token döner) | ❌ Hayır |
| `GET` | `/api/products` | Ürün listesi, arama (`?search=Apple`), filtreleme | ❌ Hayır |
| `GET` | `/api/products/:id` | Tekil ürün detay bilgisi | ❌ Hayır |
| `GET` | `/api/cart` | Kullanıcının sepet içeriği ve toplam tutar | ✅ Bearer JWT |
| `POST` | `/api/cart/items` | Sepete ürün ekleme (`{ "productId": 1, "quantity": 2 }`) | ✅ Bearer JWT |
| `POST` | `/api/checkout` | Sipariş oluşturma ve sepeti temizleme | ✅ Bearer JWT |
| `GET` | `/api/stress/delayed-response` | Yapay gecikme simülasyonu (`?delayMs=800`) | ❌ Hayır |
| `GET` | `/api/stress/heavy-computation`| Yoğun CPU hesaplama testi (`?iterations=500000`)| ❌ Hayır |
| `POST` | `/api/spike/flash-sale-buy` | Flaş İndirim stok tüketim yarışı (Spike Test) | ❌ Hayır |

---

## 3. Adım 2: Apache JMeter Kurulumu

### 3.1. macOS İçin Kurulum (Homebrew)

```bash
# 1. Homebrew ile JMeter'ı kurun
brew install jmeter

# 2. Kurulumu doğrulayın
jmeter -v

# 3. JMeter Görsel Arayüzünü (GUI) açın
jmeter
```

### 3.2. Windows / Manuel Kurulum

1. Java 17 veya üzeri kurulu olduğundan emin olun (`java -version`).
2. [Apache JMeter Resmi İndirme Sayfası](https://jmeter.apache.org/download_jmeter.cgi) adresine gidin.
3. `apache-jmeter-5.6.3.zip` dosyasını indirin ve bir klasöre çıkartın (örn: `C:\jmeter`).
4. `C:\jmeter\bin\jmeter.bat` dosyasına çift tıklayarak GUI'yi başlatın.

---

## 4. Adım 3: JMeter Test Planı Oluşturma ve API'ye Bağlama

JMeter GUI açıldığında sıfırdan bir test planı inşa etmek için şu bileşenleri adım adım ekleriz:

```
Test Plan (Hepsiburada Mock Performance Plan)
 ├── User Defined Variables (HOST=localhost, PORT=3001)
 ├── HTTP Request Defaults (Server: ${HOST}, Port: ${PORT})
 ├── HTTP Header Manager (Content-Type: application/json)
 └── Thread Group (Load Test: 50 Users)
      ├── 01_GET_HealthCheck
      │    └── Response Assertion (200 OK)
      ├── 02_POST_Login
      │    ├── JSON Extractor (Variable: authToken, Path: $.token)
      │    └── Response Assertion (200 OK)
      ├── 03_GET_ProductSearch (Path: /api/products?search=Apple)
      │    └── JSON Extractor (Variable: selectedProductId, Path: $.data[0].id)
      ├── 04_POST_AddToCart (Path: /api/cart/items)
      │    ├── Header Manager (Authorization: Bearer ${authToken})
      │    └── Response Assertion (201 Created)
      ├── 05_POST_Checkout (Path: /api/checkout)
      │    └── Header Manager (Authorization: Bearer ${authToken})
      ├── Duration Assertion (< 1000ms SLA)
      ├── Uniform Random Timer (200ms - 500ms Think Time)
      ├── View Results Tree (Hata ayıklama için)
      └── Summary Report
```

### 4.1. JMeter Bileşenleri Nelerdir ve Ne İşe Yarar? (Detaylı Anatomi)

JMeter'da eklediğimiz her bileşenin mimaride kritik bir görevi vardır. Bunları anlamadan kurulan testler ya gerçeği yansıtmaz ya da sistemi gereksiz yere kilitler:

---

#### 1. 🏗️ Test Plan (Testin Kök Konteyneri)
* **Nedir?** Tüm test senaryolarının, konfigürasyonların ve sanal kullanıcıların en tepesinde duran ana çatıdır.
* **Neden Kullanılır?** Test boyunca geçerli olacak global değişkenleri (Host, Port, Thread sayısı), global kütüphaneleri ve senaryoların sıralı mı yoksa paralel mi çalışacağını belirler.
* **Önemli Ayarları:**
  * **User Defined Variables (UDV):** Global değişkenler tanımlanır (`HOST = localhost`, `PORT = 3001`).
  * **`${__P(property_name, default_value)}` Fonksiyonu:** JMeter'ın en güçlü fonksiyonudur! Değeri hem GUI'den hem de terminalden (`-Jthreads=50`) dinamik olarak ezebilmenizi sağlar.

---

#### 2. ⚙️ HTTP Request Defaults (Merkezi İstek Şablonu - DRY Prensibi)
* **Nedir?** Test içerisindeki tüm HTTP isteklerine otomatik olarak uygulanacak varsayılan sunucu adı, port ve protokol ayarlarını tutar.
* **Neden Kullanılır?** Eğer testinizde 30 farklı HTTP isteği varsa ve sunucu adresiniz `localhost` yerine `test-api.sirket.com` olursa, 30 isteği tek tek değiştirmek zorunda kalmazsınız. Sadece buradan değiştirmeniz yeterli olur (**Don't Repeat Yourself - DRY**).
* **Önemli Ayarları:**
  * **Server Name or IP:** `${HOST}` (veya `localhost`)
  * **Port Number:** `${PORT}` (veya `3001`)
  * **Protocol:** `http` (veya `https`)

---

#### 3. 📑 HTTP Header Manager (Başlık Yöneticisi)
* **Nedir?** HTTP isteklerinin başlığına (Headers) eklenecek anahtar-değer çiftlerini tanımlar.
* **Neden Kullanılır?** REST API'ler genellikle `application/json` formatında iletişim kurar. Bu bileşen olmadan sunucuya JSON body gönderirseniz, sunucu isteği anlamayabilir ve `415 Unsupported Media Type` veya `400 Bad Request` hatası döner.
* **Önemli Ayarları:**
  * `Content-Type: application/json` -> Gönderdiğimiz verinin JSON olduğunu belirtir.
  * `Accept: application/json` -> Sunucudan JSON yanıt beklediğimizi belirtir.
  * `Authorization: Bearer ${authToken}` -> Giriş yaptıktan sonra korumalı endpoint'lere token taşır.

---

#### 4. 👥 Thread Group (Sanal Kullanıcı Grubu / Yük Motoru)
* **Nedir?** JMeter'daki "Kullanıcı" (User) kavramının karşılığıdır. Her bir **Thread (İş Parçacığı)**, sisteminize giriş yapan gerçek bir insanı simüle eder.
* **Neden Kullanılır?** Testi kaç kişinin, ne kadar sürede ve ne sıklıkla koşturacağını bu bileşenle yönetiriz.
* **Önemli Ayarları:**
  * **Number of Threads (users):** Eşzamanlı aktif kullanıcı sayısıdır (Örn: `50` kullanıcı).
  * **Ramp-up period (seconds):** Belirlenen toplam kullanıcı sayısına ne kadar sürede ulaşılacağıdır.
    * *Örnek:* 50 kullanıcı ve 10 saniye Ramp-up derseniz; JMeter her saniye sisteme `50 / 10 = 5` yeni kullanıcı sokar. Sistemi bir anda boğmamak (warm-up sağlamak) için hayati öneme sahiptir.
  * **Loop Count (Döngü Sayısı):** Her bir kullanıcının senaryoyu kaç kez tekrarlayacağıdır (`Infinite` seçilirse süre bitene kadar döner).
  * **Duration (seconds):** Testin toplam kaç saniye boyunca aktif yük basacağını belirler (Örn: `60` sn).

---

#### 5. 🌐 HTTP Request (Sampler - İstek Motoru)
* **Nedir?** Belirtilen adrese gerçek bir `GET`, `POST`, `PUT`, `DELETE` ağ isteği gönderen işçidir.
* **Neden Kullanılır?** API rotalarını (`/api/products`, `/api/auth/login`, `/api/cart/items`) çağırmak ve yanıtları toplamak için kullanılır.
* **Önemli Ayarları:**
  * **Method:** HTTP Metodu (`GET`, `POST`, vb.).
  * **Path:** Çağrılacak uç nokta (Örn: `/api/products?search=Apple`).
  * **Body Data (POST/PUT):** Gönderilecek JSON yükü (Payload).

---

#### 6. 🎣 JSON Extractor (Post-Processor - Yanıttan Veri Yakalayıcı)
* **Nedir?** Bir HTTP isteğine gelen JSON yanıtından belirli alanları (Token, ID, Fiyat vb.) JSONPath sözdizimiyle cımbızlayıp bir JMeter değişkenine kaydeden filtredir.
* **Neden Kullanılır?** **Senaryo Bağlama (Request Chaining):** Kullanıcı önce Login olur -> Gelen `token`'ı yakalar -> Sepete eklerken bu `token`'ı Authorization başlığına koyar. Bu bileşen olmadan dinamik oturum yönetimi yapılamaz!
* **Önemli Ayarları:**
  * **Names of created variables:** Değişken adı (Örn: `authToken` -> sonraki adımlarda `${authToken}` olarak kullanılır).
  * **JSON Path expressions:** Aranacak alan yolu (Örn: `$.token` veya `$.data[0].id`).
  * **Match No.:** `1` (İlk eşleşeni al) veya `0` (Rastgele birini al).
  * **Default Value:** Değer bulunamazsa atanacak varsayılan metin (`NOT_FOUND`).

---

#### 7. ✅ Response Assertion & Duration Assertion (Doğrulama ve SLA Kontrolü)
* **Nedir?** Gelen yanıtın beklediğimiz kriterlere uyup uymadığını kontrol eden kurallardır. Kural bozulursa isteği **FAIL (Kırmızı)** olarak işaretler.
* **Neden Kullanılır?** Bir sunucu hata verdiğinde `500 Internal Server Error` veya yavaşlayıp 10 saniye sonra yanıt dönebilir. Assertion'lar kalitenin güvencesidir.
* **Önemli Çeşitleri:**
  * **Response Assertion:** Yanıt Kodu `200` mi? Yanıt gövdesinde `"success": true` var mı?
  * **Duration Assertion (SLA Kontrolü):** Yanıt süresi `1000 ms` (1 saniye) altında mı? 1000ms'yi 1 milisaniye bile geçerse testi **BAŞARISIZ** sayar.

---

#### 8. ⏳ Uniform Random Timer (Think Time - Gerçek İnsan Düşünme Süresi)
* **Nedir?** İsteklerin arasına rastgele milisaniye cinsinden bekleme süresi ekleyen zamanlayıcıdır.
* **Neden Kullanılır?** Gerçek insanlar bir web sitesinde robot gibi saniyede 100 kere butona tıklamaz; ürünü inceler, 1-2 saniye düşünür ve sepete ekler. Timer eklemezseniz JMeter sunucuya **DDoS saldırısı gibi** sürekli aralıksız istek basar ve gerçeği yansıtmayan yapay bir çökme yaşanır.
* **Önemli Ayarları:**
  * **Constant Delay Offset:** `200` ms (Sabit taban bekleme).
  * **Random Delay Maximum:** `300` ms (0 ile 300 ms arasında rastgele ekleme).
  * *Sonuç:* Her istek arasında `200ms ile 500ms` arası insani gecikme oluşur.

---

#### 9. 📊 Listeners (Sonuç Dinleyicileri: View Results Tree & Summary Report)
* **Nedir?** Test koşulurken gönderilen istekleri, dönen yanıtları, hata mesajlarını ve özet istatistikleri görselleştiren panellerdir.
* **Önemli Uyarı:**
  * **View Results Tree:** Her isteğin request/response içeriğini bellekte tuttuğu için **SADECE test senaryosu yazarken / debug ederken** kullanılır. Yük testi yaparken belleği şişirir, bu yüzden yük testinde kapatılmalı ya da CLI modunda koşulmalıdır!
  * **Summary Report / Aggregate Report:** Ortalama yanıt süresi, Min/Max süre, Hata % ve Throughput (RPS) özetini verir.

---

### 4.2. Adım Adım JMeter GUI Ekranında Ne Yapacaksınız? (Tıklama Tıklama Uygulama Rehberi)

JMeter arayüzünü açtığınızda (`jmeter` komutu ile) sıfırdan yukarıdaki test planını inşa etmek için şu adımları fareyle sırasıyla uygulayın:

---

#### 🖱️ Adım 1: Test Plan Ayarlarını ve Dinamik Değişkenleri Yapılandırma
1. Sol paneldeki en üstte bulunan **`Test Plan`** yazısına sol tıklayın.
2. Sağ tarafta açılan ekranda **Test Plan Name** kısmına `Hepsiburada Mock E-Commerce Performance Test Plan` yazın.
3. Aşağıdaki **User Defined Variables** tablosunun altındaki **`Add`** butonuna 5 defa tıklayarak şu satırları ekleyin:

| Name (Değişken Adı) | Value (Değeri) | Ne Anlama Geliyor? |
|---|---|---|
| `HOST` | `${__P(host,localhost)}` | CLI'dan `-Jhost` girilmezse `localhost` al |
| `PORT` | `${__P(port,3001)}` | CLI'dan `-Jport` girilmezse `3001` al |
| `THREADS` | `${__P(threads,10)}` | CLI'dan `-Jthreads` girilmezse `10` kullanıcı yap |
| `RAMP_UP` | `${__P(rampUp,5)}` | Kullanıcıları `5` saniyede sisteme sok |
| `DURATION` | `${__P(duration,30)}` | Testi `30` saniye boyunca koştur |

---

#### 🖱️ Adım 2: Ortak Sunucu Ayarlarını Tanımlama (HTTP Request Defaults)
1. Sol paneldeki **`Test Plan`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Config Element`** ➡️ **`HTTP Request Defaults`** seçin.
3. Sağ taraftaki ekranda şu alanları doldurun:
   * **Protocol [http]:** `http`
   * **Server Name or IP:** `${HOST}`
   * **Port Number:** `${PORT}`
   * **Content encoding:** `UTF-8`

---

#### 🖱️ Adım 3: Ortak JSON Başlıklarını Tanımlama (HTTP Header Manager)
1. Sol paneldeki **`Test Plan`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Config Element`** ➡️ **`HTTP Header Manager`** seçin.
3. Sağ taraftaki tablonun altındaki **`Add`** butonuna tıklayarak şu 2 satırı ekleyin:
   * Name: `Content-Type` ➡️ Value: `application/json`
   * Name: `Accept` ➡️ Value: `application/json`

---

#### 🖱️ Adım 4: Kullanıcı Yük Grubunu Oluşturma (Thread Group)
1. Sol paneldeki **`Test Plan`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Threads (Users)`** ➡️ **`Thread Group`** seçin.
3. Sağ ekranda şu ayarları yapın:
   * **Name:** `TG - E-Commerce User Journey (Load Test)`
   * **Action to be taken after a Sampler error:** `Continue`
   * **Number of Threads (users):** `${THREADS}`
   * **Ramp-up period (seconds):** `${RAMP_UP}`
   * **Loop Count:** **`Infinite`** kutucuğunu işaretleyin (veya `-1` yazın).
   * **Specify Thread lifetime:** Kutucuğunu **işaretleyin**.
   * **Duration (seconds):** `${DURATION}`

---

#### 🖱️ Adım 5: 1. İstek - Health Check (GET /api/health)
1. Sol paneldeki **`TG - E-Commerce User Journey`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Sampler`** ➡️ **`HTTP Request`** seçin.
3. Alanları doldurun:
   * **Name:** `01_GET_HealthCheck`
   * **HTTP Method:** `GET`
   * **Path:** `/api/health`
4. *Doğrulama (Assertion) Ekleyelim:*
   * Az önce oluşturduğunuz **`01_GET_HealthCheck`** öğesine **sağ tıklayın**.
   * **`Add`** ➡️ **`Assertions`** ➡️ **`Response Assertion`** seçin.
   * **Field to Test:** `Response Code` seçin.
   * **Pattern Matching Rules:** `Equals` seçin.
   * **Patterns to Test** tablosundaki **`Add`** butonuna basıp `200` yazın.

---

#### 🖱️ Adım 6: 2. İstek - Login & Token Yakalama (POST /api/auth/login)
1. **`Thread Group`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Sampler`** ➡️ **`HTTP Request`** seçin.
3. Alanları doldurun:
   * **Name:** `02_POST_Login`
   * **HTTP Method:** `POST`
   * **Path:** `/api/auth/login`
   * **Body Data** sekmesine tıklayın ve şu JSON'ı yapıştırın:
     ```json
     {
       "username": "qa_tester",
       "password": "Pass1234!"
     }
     ```
4. *Gelen Token'ı Yakalayalım (JSON Extractor):*
   * **`02_POST_Login`** öğesine **sağ tıklayın**.
   * **`Add`** ➡️ **`Post Processors`** ➡️ **`JSON Extractor`** seçin.
   * **Name of created variables:** `authToken`
   * **JSON Path expressions:** `$.token`
   * **Match No. (0 for Random):** `1`
   * **Default Values:** `TOKEN_NOT_FOUND`

---

#### 🖱️ Adım 7: 3. İstek - Ürün Arama & Ürün ID Yakalama (GET /api/products)
1. **`Thread Group`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Sampler`** ➡️ **`HTTP Request`** seçin.
3. Alanları doldurun:
   * **Name:** `03_GET_ProductSearch`
   * **HTTP Method:** `GET`
   * **Path:** `/api/products?search=Apple`
4. *İlk Ürünün ID'sini Yakalayalım:*
   * **`03_GET_ProductSearch`** öğesine **sağ tıklayın**.
   * **`Add`** ➡️ **`Post Processors`** ➡️ **`JSON Extractor`** seçin.
   * **Name of created variables:** `selectedProductId`
   * **JSON Path expressions:** `$.data[0].id`
   * **Match No.:** `1`
   * **Default Values:** `1`

---

#### 🖱️ Adım 8: 4. İstek - Sepete Ekleme & Token Gönderme (POST /api/cart/items)
1. **`Thread Group`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Sampler`** ➡️ **`HTTP Request`** seçin.
3. Alanları doldurun:
   * **Name:** `04_POST_AddToCart`
   * **HTTP Method:** `POST`
   * **Path:** `/api/cart/items`
   * **Body Data:**
     ```json
     {
       "productId": ${selectedProductId},
       "quantity": 1
     }
     ```
4. *Bu İsteğe Özel Authorization Başlığı Ekleyelim:*
   * **`04_POST_AddToCart`** öğesine **sağ tıklayın**.
   * **`Add`** ➡️ **`Config Element`** ➡️ **`HTTP Header Manager`** seçin.
   * Tabloya **`Add`** deyip ekleyin:
     * Name: `Authorization`
     * Value: `Bearer ${authToken}`

---

#### 🖱️ Adım 9: 5. İstek - Sipariş Verme / Checkout (POST /api/checkout)
1. **`Thread Group`** öğesine **sağ tıklayın**.
2. **`Add`** ➡️ **`Sampler`** ➡️ **`HTTP Request`** seçin.
   * **Name:** `05_POST_Checkout`
   * **HTTP Method:** `POST`
   * **Path:** `/api/checkout`
3. *Authorization Başlığı Ekleyelim:*
   * **`05_POST_Checkout`** öğesine **sağ tıklayın** ➡️ **`Add`** ➡️ **`Config Element`** ➡️ **`HTTP Header Manager`**.
   * Name: `Authorization`, Value: `Bearer ${authToken}` ekleyin.

---

#### 🖱️ Adım 10: SLA Süre Kontrolü ve Düşünme Süresi (Timer) Ekleyelim
1. **`Thread Group`** öğesine **sağ tıklayın** ➡️ **`Add`** ➡️ **`Assertions`** ➡️ **`Duration Assertion`**:
   * **Duration in milliseconds:** `1000` *(1000ms'den uzun süren istekler hata verir).*
2. **`Thread Group`** öğesine **sağ tıklayın** ➡️ **`Add`** ➡️ **`Timer`** ➡️ **`Uniform Random Timer`**:
   * **Constant Delay Offset (in milliseconds):** `200`
   * **Random Delay Maximum (in milliseconds):** `300` *(200ms-500ms arası insani bekleme).*

---

#### 🖱️ Adım 11: Sonuç Ekranlarını (Listeners) Ekleyelim
1. **`Thread Group`** öğesine **sağ tıklayın** ➡️ **`Add`** ➡️ **`Listener`** ➡️ **`View Results Tree`**.
2. **`Thread Group`** öğesine **sağ tıklayın** ➡️ **`Add`** ➡️ **`Listener`** ➡️ **`Summary Report`**.

---

#### 🖱️ Adım 12: Testi Çalıştırma ve Doğrulama
1. Mock API sunucunuzun çalıştığından emin olun (`node performance-test/mock-api/server.js`).
2. JMeter'ın üst araç çubuğundaki yeşil **Start (▶️)** butonuna tıklayın.
3. Sol panelden **`View Results Tree`** öğesine tıklayın. İsteklerin sırasıyla yeşil yandığını, `02_POST_Login`'den dönen token'ın `04_POST_AddToCart` ve `05_POST_Checkout` isteklerine sorunsuz aktarıldığını gözlerinizle görün.
4. Üst menüden **`File`** ➡️ **`Save As`** diyerek dosyanızı `performance-test/jmeter/ecommerce_api_test_plan.jmx` yoluna kaydedin!

---

## 5. Adım 4: Test Türleri ve Konfigürasyonları

Performans testleri amaçlarına göre 4 ana kategoriye ayrılır:

```
Kullanıcı Sayısı
     ▲
 500 ┤                   ┌───┐ (Spike Test: Ani Sıçrama)
     │                   │   │
 300 ┤             ┌─────┘   └─────┐ (Stress Test: Zorlama)
     │       ┌─────┘               └─────┐
 100 ┤ ┌─────┘ (Load Test: Beklenen Yük) └─────┐
     │ │                                        │
  0  └─┴────────────────────────────────────────┴────────► Zaman (Dakika)
```

### 5.1. 1. Load Test (Normal Yük Testi)
- **Amaç:** Sistemin normal iş gününde veya beklenen ortalama trafikte SLA değerlerini (örn: Yanıt süresi < 500ms) koruyup korumadığını doğrulamak.
- **Konfigürasyon:**
  - **Thread (Kullanıcı):** 50 - 100
  - **Ramp-up (Isınma):** 30 - 60 saniye
  - **Duration (Süre):** 10 - 30 dakika

### 5.2. 2. Stress Test (Zorlama / Kırılma Noktası Testi)
- **Amaç:** Sistemin maksimum kapasitesini ve çöktüğü (hata oranının %10'u geçtiği veya yanıt sürelerinin 3-5 saniyeye çıktığı) **Kırılma Noktasını (Breaking Point)** tespit etmek.
- **Konfigürasyon:**
  - Kademeli artış: 50 -> 100 -> 250 -> 500 -> 1000 kullanıcı.
  - Hedef: Mock API'nin `/api/stress/heavy-computation` endpoint'ine yoğun yük bindirerek CPU tıkanmasını izlemek.

### 5.3. 3. Spike Test (Ani Yük Sıçraması Testi)
- **Amaç:** "Efsane Cuma", "Flaş İndirim" veya push bildirim sonrası anlık 10 katına çıkan trafikte sistemin çökmeden toparlanıp toparlanmadığını görmek.
- **Konfigürasyon:**
  - **Thread:** 300 - 500 kullanıcı
  - **Ramp-up:** Sadece 2 - 5 saniye!
  - **Duration:** 1 - 2 dakika
  - **Hedef Endpoint:** `/api/spike/flash-sale-buy` (Stok yarışı simülasyonu).

### 5.4. 4. Endurance / Soak Test (Dayanıklılık Testi)
- **Amaç:** Uzun süreli yük altında sistemde **Bellek Sızıntısı (Memory Leak)**, açık kalan veritabanı bağlantıları veya şişen log dosyalarını yakalamak.
- **Konfigürasyon:**
  - **Thread:** 30 - 50 kullanıcı (Orta yük)
  - **Duration:** 2 saat - 12 saat

---

## 6. Adım 5: CLI (Non-GUI) ile Koşum ve Zengin HTML Dashboard Raporu Üretme

> [!IMPORTANT]
> ### 🛑 Neden Asla GUI Modunda Yük Testi Koşulmaz?
> JMeter GUI arayüzü çizimler, tablolar ve pencereler için yoğun miktarda bellek (JVM Heap) ve CPU tüketir. Yük testini GUI'den koşarsanız **bilgisayarınız tıkanır** ve elde ettiğiniz yanıt süreleri gerçeği yansıtmaz.  
> **Kural:** Test planı GUI'de hazırlanır `.jmx` olarak kaydedilir; asıl test **her zaman CLI (Command Line)** üzerinden koşturulur!

### 6.1. Tek Komutla Test Koşumu ve HTML Rapor Üretimi

Proje kök dizinindeyken terminalden şu komutu çalıştırın:

```bash
# 1. Eski rapor klasörü varsa temizleyin
rm -rf performance-test/jmeter/results.jtl performance-test/jmeter/html-report

# 2. CLI üzerinden 20 sanal kullanıcı ile 30 saniye koşturup HTML raporu üretin
jmeter -n \
  -t performance-test/jmeter/ecommerce_api_test_plan.jmx \
  -l performance-test/jmeter/results.jtl \
  -e -o performance-test/jmeter/html-report \
  -Jhost=localhost \
  -Jport=3001 \
  -Jthreads=20 \
  -JrampUp=5 \
  -Jduration=30
```

### 6.2. CLI Parametrelerinin Anlamı

| Parametre | Anlamı | Açıklama |
|---|---|---|
| `-n` | **Non-GUI Mode** | Arayüzsüz, maksimum performanslı CLI modunda çalıştırır. |
| `-t` | **Test Plan Path** | Çalıştırılacak `.jmx` dosyasının yolu. |
| `-l` | **Log / Results File** | Ham sonuçların yazılacağı `.jtl` (veya `.csv`) dosyası. |
| `-e` | **Generate Dashboard** | Test bittiğinde interaktif HTML Dashboard üretir. |
| `-o` | **Output Folder** | Üretilen HTML raporun kaydedileceği klasör. |
| `-Jparam=val`| **Dynamic Properties**| JMeter içerisindeki `${__P(param)}` değişkenlerini dışarıdan ezme. |

### 6.3. Raporu Tarayıcıda Görüntüleme

Test tamamlandıktan sonra üretilen interaktif raporu tarayıcınızda açın:

```bash
open performance-test/jmeter/html-report/index.html
```

---

## 7. Adım 6: Performans Metriklerini Okuma ve Yorumlama

JMeter HTML Dashboard raporunda dikkat etmeniz gereken 4 altın metrik:

### 7.1. APDEX Skoru (Application Performance Index)
- **0.0 ile 1.0** arasında bir kullanıcı memnuniyet puanıdır.
- `T (Tolerable Time)`: Kabul edilebilir süre (Varsayılan 500ms).
- `F (Frustrated Time)`: Kullanıcının siteden çıktığı süre (Varsayılan 1500ms).
- **Skor Yorumu:**
  - `0.94 - 1.00`: 🟢 **Mükemmel (Excellent)**
  - `0.85 - 0.93`: 🟡 **İyi (Good)**
  - `< 0.70`: 🔴 **Kabul Edilemez (Unacceptable - Acil Optimizasyon Gerekli)**

### 7.2. Response Time Percentiles (Yüzdelik Dilimler)
Ortalama (Average) yanıt süresi yanıltıcıdır! 1000 isteğin 999'u 50ms, 1 tanesi 10 saniye sürerse ortalama düşük görünür ama o 1 müşteri sepeti terk eder. Bu yüzden **Percentile** değerlerine bakılır:
- **90th Percentile (P90):** İsteklerin %90'ının tamamlandığı maksimum süre.
- **95th Percentile (P95):** Kurumsal SLA standardı (Genellikle < 500ms hedeflenir).
- **99th Percentile (P99):** En kötü durumdaki %1'lik marjinal kullanıcı deneyimi.

### 7.3. Throughput / RPS (Requests Per Second)
- Sunucunun saniyede kaç isteği başarıyla karşılayabildiğini gösterir.
- Kullanıcı sayısı arttıkça Throughput artmıyor ve sabit kalıyorsa veya düşüyorsa, sistem **darboğaza (bottleneck)** girmiştir.

### 7.4. Error % (Hata Oranı)
- Yük altında HTTP 500, 502, 504 veya Assertion Failure oranını gösterir.
- **Kabul Edilebilir Limit:** Normal yük testinde **%0.00**, stres testinde maksimum **< %1.00**.

---

## 8. Adım 7: Jenkins CI/CD Pipeline Entegrasyonu

Performans testlerini her gece veya sürüm öncesinde Jenkins üzerinde otomatik olarak koşturup raporlamak için `Jenkinsfile` pipeline örneği:

```groovy
pipeline {
    agent any

    parameters {
        string(name: 'JMETER_THREADS', defaultValue: '30', description: 'Eşzamanlı Kullanıcı Sayısı')
        string(name: 'JMETER_DURATION', defaultValue: '60', description: 'Test Süresi (Saniye)')
    }

    stages {
        stage('Start Mock Backend') {
            steps {
                echo 'Starting Mock E-Commerce API in Background...'
                sh 'node performance-test/mock-api/server.js &'
                sh 'sleep 2'
            }
        }

        stage('Execute JMeter Performance Tests') {
            steps {
                echo "Running JMeter Performance Test with ${params.JMETER_THREADS} users for ${params.JMETER_DURATION}s..."
                sh """
                    rm -rf performance-test/jmeter/results.jtl performance-test/jmeter/html-report
                    jmeter -n \
                        -t performance-test/jmeter/ecommerce_api_test_plan.jmx \
                        -l performance-test/jmeter/results.jtl \
                        -e -o performance-test/jmeter/html-report \
                        -Jhost=localhost \
                        -Jport=3001 \
                        -Jthreads=${params.JMETER_THREADS} \
                        -Jduration=${params.JMETER_DURATION}
                """
            }
        }

        stage('Publish Performance Dashboard') {
            steps {
                // Publish HTML Report to Jenkins UI
                publishHTML([
                    allowMissing: false,
                    alwaysLinkToLastBuild: true,
                    keepAll: true,
                    reportDir: 'performance-test/jmeter/html-report',
                    reportFiles: 'index.html',
                    reportName: 'JMeter Performance Dashboard'
                ])
            }
        }
    }

    post {
        always {
            // Ham JTL log dosyasını arşivle
            archiveArtifacts artifacts: 'performance-test/jmeter/results.jtl', fingerprint: true
        }
    }
}
```

---

## 9. Proje Dosya Ağacı ve Hazır Şablonlar

Projenizde performans testi için oluşturulan yapının klasör hiyerarşisi:

```text
performance-test/
├── mock-api/                        # Sıfır Bağımlılık Local Mock Backend
│   ├── server.js                    # Node.js Native HTTP API (Tüm E-Ticaret Route'ları)
│   ├── package.json                 # Opsiyonel scriptler
│   └── Dockerfile                   # Docker container yapılandırması
│
└── jmeter/                          # JMeter Test Varlıkları
    ├── ecommerce_api_test_plan.jmx  # Hazır, parametrik JMeter Test Planı
    ├── test_data/                   # Data-Driven Test Verileri (CSV)
    │   ├── users.csv                # Kullanıcı giriş verileri
    │   └── search_queries.csv       # Arama ve kategori sorguları
    ├── results.jtl                  # Test koşum log çıktısı (Otomatik üretilir)
    └── html-report/                 # İnteraktif HTML Raporu (Otomatik üretilir)
```

---

## 💡 Altın Tavsiyeler

1. **Önce 1 Kullanıcı ile Doğrulayın:** Yük testine 500 kullanıcıyla başlamayın. Önce `threads=1` ve `loops=1` ile tüm senaryonun 200/201 aldığını doğrulayın.
2. **Kademeli Isınma (Ramp-Up):** 100 kullanıcıyı anında (`rampUp=0`) başlatmayın; en az 30-60 saniyeye yayarak sunucunun thread havuzunun ısınmasını sağlayın.
3. **Assertion'ları Abartmayın:** Her istekte 10 tane karmaşık regex assertion koymak JMeter'ın kendi işlemci yükünü artırır. Sadece durum kodu (`200 OK`) ve temel gövde varlığını doğrulamak yeterlidir.
