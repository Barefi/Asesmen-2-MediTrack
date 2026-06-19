import { errorResponse } from "@/lib/http";
import { prisma } from "@/lib/prisma";
import { readImageFile } from "@/lib/storage";

export async function privateImageResponse(ownerEmail: string, imageId: string) {
  const image = await prisma.imageAsset.findFirst({
    where: {
      id: imageId,
      ownerEmail
    }
  });

  if (!image) {
    return errorResponse("NOT_FOUND", "Gambar tidak ditemukan atau bukan milik user.", 404);
  }

  try {
    const bytes = await readImageFile(image.storageKey);

    return new Response(bytes, {
      headers: {
        "Content-Type": image.mimeType,
        "Content-Length": image.size.toString(),
        "Cache-Control": "private, max-age=300"
      }
    });
  } catch (error) {
    console.error("Image read failed", error);
    return errorResponse("IMAGE_NOT_FOUND", "File gambar tidak ditemukan di storage.", 404);
  }
}
