type MedicineRow = {
  id?: string;
  nama: string;
  detail: string;
  imageId: string | null;
};

type WireMedicine = {
  id?: string;
  nama: string;
  detail: string;
  namaLatin: string;
  imageId: string | null;
};

export function toAndroidMedicine(row: MedicineRow): WireMedicine {
  return {
    id: row.id,
    nama: row.nama,
    detail: row.detail,
    namaLatin: row.detail,
    imageId: row.imageId
  };
}

export function toMutationMedicine(row: Required<Pick<MedicineRow, "id">> & MedicineRow) {
  return {
    id: row.id,
    nama: row.nama,
    detail: row.detail,
    namaLatin: row.detail,
    imageId: row.imageId
  };
}
