# MediTrack API

REST API Next.js untuk fitur cloud aplikasi Android MediTrack.

Endpoint utama adalah `/obat`. Endpoint lama `/hewan.php` dan `/image.php?id=...` tetap tersedia hanya sebagai alias kompatibilitas modul/Android lama.

## Setup

```bash
cd backend
npm install
copy .env.example .env
npm run prisma:generate
npm run prisma:migrate
npm run dev
```

Isi `.env`:

```properties
DATABASE_URL="postgresql://postgres:postgres@localhost:5432/meditrack?schema=public"
UPLOAD_DIR="./uploads"
MAX_UPLOAD_BYTES="5242880"
```

Base URL untuk Android saat development:

```properties
API_BASE_URL="http://10.0.2.2:3000/"
```

Gunakan `10.0.2.2` untuk Android Emulator. Untuk device fisik, gunakan IP komputer di jaringan lokal.

## Auth MVP

Android mengirim header:

```http
Authorization: user@email.com
```

Semua query memfilter `ownerEmail` dari header ini. Jangan menerima `ownerEmail` dari body request.

Catatan produksi: Android sebaiknya mengirim Google ID token, backend memverifikasi token Google, lalu mengambil email dari token valid.

## Endpoint

### GET `/obat`

Response sukses berupa array langsung:

```json
[
  {
    "id": "uuid-row-id",
    "nama": "Paracetamol",
    "detail": "500mg - 08:00",
    "namaLatin": "500mg - 08:00",
    "imageId": "uuid-file-id"
  }
]
```

`namaLatin` hanya alias legacy. Gunakan `detail` untuk kode baru. `id` adalah id row obat dan dipakai untuk operasi delete saat obat tidak punya gambar.

### POST `/obat`

Multipart form fields:

- `nama`: wajib
- `detail`: wajib
- `namaLatin` atau `jadwal`: diterima sebagai alias legacy
- `image`: opsional, menerima alias `gambar` atau `file`

Response:

```json
{
  "success": true,
  "data": {
    "id": "uuid-row-id",
    "nama": "Paracetamol",
    "detail": "500mg - 08:00",
    "namaLatin": "500mg - 08:00",
    "imageId": "uuid-file-id"
  }
}
```

### DELETE `/obat?id={idOrImageId}`

Menghapus data milik user. `id` boleh row id atau imageId agar kompatibel dengan Android yang mengirim `imageId`.

### GET `/images/{imageId}`

Mengambil gambar milik user yang sama. Gambar tidak public tanpa validasi header.

### GET `/image.php?id={imageId}`

Endpoint legacy untuk Android saat ini. Behavior sama dengan `/images/{imageId}`.

### Legacy `/hewan.php`

`GET`, `POST`, dan `DELETE` `/hewan.php` tetap diarahkan ke handler obat. Pakai `/obat` untuk kode baru.

## Curl

GET:

```bash
curl -H "Authorization: user@email.com" http://localhost:3000/obat
```

POST tanpa gambar:

```bash
curl -X POST http://localhost:3000/obat \
  -H "Authorization: user@email.com" \
  -F "nama=Paracetamol" \
  -F "detail=500mg - 08:00"
```

POST dengan gambar:

```bash
curl -X POST http://localhost:3000/obat \
  -H "Authorization: user@email.com" \
  -F "nama=Paracetamol" \
  -F "detail=500mg - 08:00" \
  -F "image=@./sample.jpg"
```

DELETE:

```bash
curl -X DELETE -H "Authorization: user@email.com" \
  "http://localhost:3000/obat?id=uuid-file-id"
```

Image:

```bash
curl -H "Authorization: user@email.com" \
  "http://localhost:3000/images/uuid-file-id" --output obat.jpg
```

Legacy image:

```bash
curl -H "Authorization: user@email.com" \
  "http://localhost:3000/image.php?id=uuid-file-id" --output obat.jpg
```
