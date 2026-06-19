type AndroidMedicine = {
  nama: string;
  namaLatin: string;
  imageId: string | null;
};

type MedicineRow = AndroidMedicine & {
  id: string;
};

export function toAndroidMedicine(row: AndroidMedicine): AndroidMedicine {
  return {
    nama: row.nama,
    namaLatin: row.namaLatin,
    imageId: row.imageId
  };
}

export function toMutationMedicine(row: MedicineRow) {
  return {
    id: row.id,
    nama: row.nama,
    namaLatin: row.namaLatin,
    imageId: row.imageId
  };
}
