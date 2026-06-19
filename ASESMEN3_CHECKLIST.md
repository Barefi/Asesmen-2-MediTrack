# Asesmen 3 - MediTrack

## Ringkasan aplikasi

MediTrack dikembangkan dari aplikasi jadwal obat lokal menjadi mini project yang menggabungkan:

- Room database untuk jadwal obat lokal, status minum, recycle bin, dan dark mode.
- REST API dinamis untuk catatan cloud pengguna.
- Login Google dengan Credential Manager dan penyimpanan profil di DataStore.
- Upload foto dan teks ke server menggunakan multipart request.
- Tampilan data JSON dan gambar dari internet dengan loading, error, empty state, dan retry.

## Mapping rubrik asesmen

| Syarat / penilaian | Implementasi |
| --- | --- |
| Kotlin dan Jetpack Compose | Seluruh UI memakai Compose. |
| Minimum SDK API 26 | `minSdk = 26` di `app/build.gradle.kts`. |
| REST API dinamis | Retrofit + Moshi memakai `BuildConfig.API_BASE_URL`, default `https://gh.d3ifcool.org/`. |
| Login/logout | `ProfileDialog` memakai Google Credential Manager dan `AuthActions`. |
| Login persisten | `UserPreferences` menyimpan nama, email, dan foto profil di DataStore. |
| Profil circle | Dialog profil menampilkan foto pengguna berbentuk circle, nama, dan email. |
| Ambil JSON + gambar | `CloudMedicationScreen` mengambil list dari API dan menampilkan gambar dengan Coil. |
| Kirim gambar + teks | FAB Cloud membuka kamera, dialog mengisi teks, lalu ViewModel mengirim multipart POST. |
| Hapus data | Data cloud dapat dihapus lewat tombol delete dan dialog konfirmasi. |
| Loading dan error internet | `ApiStatus` menampilkan progress, retry, dan pesan error ketika request gagal. |
| Kebaruan | Aplikasi menggabungkan Room lokal yang sudah ada dengan fitur REST cloud berbasis user. |
| Commit bertahap | Branch ini berisi minimal 12 commit task implementasi. |

## Referensi modul

- Modul 12: Connect to Internet - Retrofit, Moshi, Coil, JSON, gambar dari internet.
- Modul 13: Internet Best Practices - loading indicator dan penanganan error koneksi.
- Modul 14: Authenticate User - Google Sign-In, DataStore, profil, logout.
- Modul 15: Sending Data to Server - kamera, input dialog, multipart POST, data sesuai user.

## Setup sebelum demo

Isi `local.properties` dengan Web Client ID Google:

```properties
API_KEY="web_client_id_google"
API_BASE_URL="https://gh.d3ifcool.org/"
```

`API_BASE_URL` boleh tidak diisi karena sudah ada default dari build config.
