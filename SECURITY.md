# 🔒 Security Policy (Güvenlik Politikası)

## Desteklenen Sürümler

Aşağıdaki sürümler aktif olarak güvenlik güncellemelerini almaktadır:

| Sürüm | Destek Durumu |
|---|---|
| 1.0-SNAPSHOT (main) | :white_check_mark: Destekleniyor |

---

## 🛡️ Güvenlik Açığı Bildirimi (Reporting a Vulnerability)

Bu projede veya bağımlılıklarında potansiyel bir güvenlik açığı tespit ettiyseniz:

1. **Lütfen herkese açık GitHub Issue açmayınız.**
2. Açık detaylarını ve reproduksiyon adımlarını `security@hepsiburada.mock` adresine e-posta ile iletiniz.
3. Bildiriminiz 48 saat içinde incelenecek ve gerekli yama yayınlanacaktır.

---

## ⚙️ Güvenlik İlkeleri
- Asla gerçek kullanıcı şifresi veya üretim API anahtarlarını repoya commit etmeyiniz.
- Gizli değişkenler için `.env` veya CI Secrets mekanizmalarını kullanınız.
