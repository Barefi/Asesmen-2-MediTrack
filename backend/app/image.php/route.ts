import { getOwnerEmail } from "@/lib/auth";
import { errorResponse } from "@/lib/http";
import { privateImageResponse } from "@/lib/imageResponse";

export const runtime = "nodejs";

export async function GET(request: Request) {
  const ownerEmail = getOwnerEmail(request);
  if (!ownerEmail) {
    return errorResponse("UNAUTHORIZED", "Header Authorization harus berisi email user.", 401);
  }

  const imageId = new URL(request.url).searchParams.get("id")?.trim();
  if (!imageId) {
    return errorResponse("VALIDATION_ERROR", "Query id wajib diisi.", 422);
  }

  return privateImageResponse(ownerEmail, imageId);
}
