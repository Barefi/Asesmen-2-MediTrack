import { NextResponse } from "next/server";
import { getOwnerEmail } from "@/lib/auth";
import { getOptionalImageFile, getRequiredString } from "@/lib/form";
import { errorResponse, successResponse } from "@/lib/http";
import { toAndroidMedicine, toMutationMedicine } from "@/lib/medicine";
import { prisma } from "@/lib/prisma";
import {
  deleteImageFile,
  saveUploadedImage,
  UploadValidationError,
  type SavedImageFile
} from "@/lib/storage";

export const runtime = "nodejs";

export async function GET(request: Request) {
  const ownerEmail = getOwnerEmail(request);
  if (!ownerEmail) {
    return errorResponse("UNAUTHORIZED", "Header Authorization harus berisi email user.", 401);
  }

  const medicines = await prisma.cloudMedicine.findMany({
    where: { ownerEmail },
    orderBy: { createdAt: "desc" },
    select: {
      nama: true,
      namaLatin: true,
      imageId: true
    }
  });

  // Android lama memakai Retrofit/Moshi yang mengharapkan array langsung.
  return NextResponse.json(medicines.map(toAndroidMedicine));
}

export async function POST(request: Request) {
  const ownerEmail = getOwnerEmail(request);
  if (!ownerEmail) {
    return errorResponse("UNAUTHORIZED", "Header Authorization harus berisi email user.", 401);
  }

  let formData: FormData;
  try {
    formData = await request.formData();
  } catch {
    return errorResponse("INVALID_MULTIPART", "Body harus berupa multipart/form-data.", 400);
  }

  const nama = getRequiredString(formData, "nama");
  const namaLatin = getRequiredString(formData, "namaLatin");
  if (!nama || !namaLatin) {
    return errorResponse("VALIDATION_ERROR", "Field nama dan namaLatin wajib diisi.", 422);
  }

  let savedImage: SavedImageFile | null = null;

  try {
    const imageFile = getOptionalImageFile(formData);
    if (imageFile) {
      savedImage = await saveUploadedImage(ownerEmail, imageFile);
    }

    const medicine = await prisma.$transaction(async (tx) => {
      if (savedImage) {
        await tx.imageAsset.create({
          data: {
            id: savedImage.id,
            ownerEmail,
            storageKey: savedImage.storageKey,
            originalName: savedImage.originalName,
            mimeType: savedImage.mimeType,
            size: savedImage.size
          }
        });
      }

      return tx.cloudMedicine.create({
        data: {
          ownerEmail,
          nama,
          namaLatin,
          imageId: savedImage?.id ?? null
        },
        select: {
          id: true,
          nama: true,
          namaLatin: true,
          imageId: true
        }
      });
    });

    return successResponse(toMutationMedicine(medicine), 201);
  } catch (error) {
    if (savedImage) {
      await deleteImageFile(savedImage.storageKey);
    }

    if (error instanceof UploadValidationError) {
      return errorResponse(error.code, error.message, error.status);
    }

    console.error("POST /hewan.php failed", error);
    return errorResponse("INTERNAL_ERROR", "Data obat gagal disimpan.", 500);
  }
}

export async function DELETE(request: Request) {
  const ownerEmail = getOwnerEmail(request);
  if (!ownerEmail) {
    return errorResponse("UNAUTHORIZED", "Header Authorization harus berisi email user.", 401);
  }

  const id = new URL(request.url).searchParams.get("id")?.trim();
  if (!id) {
    return errorResponse("VALIDATION_ERROR", "Query id wajib diisi.", 422);
  }

  const medicine = await prisma.cloudMedicine.findFirst({
    where: {
      ownerEmail,
      OR: [{ id }, { imageId: id }]
    },
    select: {
      id: true,
      imageId: true
    }
  });

  if (!medicine) {
    return errorResponse("NOT_FOUND", "Data tidak ditemukan atau bukan milik user.", 404);
  }

  const image = medicine.imageId
    ? await prisma.imageAsset.findFirst({
        where: { id: medicine.imageId, ownerEmail },
        select: { id: true, storageKey: true }
      })
    : null;

  try {
    await prisma.$transaction(async (tx) => {
      await tx.cloudMedicine.delete({ where: { id: medicine.id } });

      if (image) {
        await tx.imageAsset.delete({ where: { id: image.id } });
      }
    });

    if (image) {
      await deleteImageFile(image.storageKey);
    }

    return successResponse({ id: medicine.id, imageId: medicine.imageId });
  } catch (error) {
    console.error("DELETE /hewan.php failed", error);
    return errorResponse("INTERNAL_ERROR", "Data obat gagal dihapus.", 500);
  }
}
