import { getOwnerEmail } from "@/lib/auth";
import { errorResponse } from "@/lib/http";
import { privateImageResponse } from "@/lib/imageResponse";

export const runtime = "nodejs";

type ImageRouteContext = {
  params: Promise<{
    imageId: string;
  }>;
};

export async function GET(request: Request, { params }: ImageRouteContext) {
  const ownerEmail = getOwnerEmail(request);
  if (!ownerEmail) {
    return errorResponse("UNAUTHORIZED", "Header Authorization harus berisi email user.", 401);
  }

  const { imageId } = await params;
  return privateImageResponse(ownerEmail, imageId);
}
