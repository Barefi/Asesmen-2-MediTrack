# MediTrack API

REST API Next.js untuk fitur cloud aplikasi Android MediTrack.

Endpoint sengaja mempertahankan nama lama `/hewan.php` dan `/image.php?id=...` agar kompatibel dengan Retrofit/Moshi yang sudah ada di Android.

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

TODO produksi: Android sebaiknya mengirim Google ID token, backend memverifikasi token Google, lalu mengambil email dari token valid.

## Endpoint

### GET `/hewan.php`

Response sukses berupa array langsung:

```json
[
  {
    "nama": "Paracetamol",
    "namaLatin": "500mg - 08:00",
    "imageId": "uuid-file-id"
  }
]
```

### POST `/hewan.php`

Multipart form fields:

- `nama`: wajib
- `namaLatin`: wajib
- `image`: opsional, menerima alias `gambar` atau `file`

Response:

```json
{
  "success": true,
  "data": {
    "id": "uuid-row-id",
    "nama": "Paracetamol",
    "namaLatin": "500mg - 08:00",
    "imageId": "uuid-file-id"
  }
}
```

### DELETE `/hewan.php?id={idOrImageId}`

Menghapus data milik user. `id` boleh row id atau imageId agar kompatibel dengan Android yang mengirim `imageId`.

### GET `/images/{imageId}`

Mengambil gambar milik user yang sama. Gambar tidak public tanpa validasi header.

### GET `/image.php?id={imageId}`

Endpoint legacy untuk Android saat ini. Behavior sama dengan `/images/{imageId}`.

## Curl

GET:

```bash
curl -H "Authorization: user@email.com" http://localhost:3000/hewan.php
```

POST tanpa gambar:

```bash
curl -X POST http://localhost:3000/hewan.php \
  -H "Authorization: user@email.com" \
  -F "nama=Paracetamol" \
  -F "namaLatin=500mg - 08:00"
```

POST dengan gambar:

```bash
curl -X POST http://localhost:3000/hewan.php \
  -H "Authorization: user@email.com" \
  -F "nama=Paracetamol" \
  -F "namaLatin=500mg - 08:00" \
  -F "image=@./sample.jpg"
```

DELETE:

```bash
curl -X DELETE -H "Authorization: user@email.com" \
  "http://localhost:3000/hewan.php?id=uuid-file-id"
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
