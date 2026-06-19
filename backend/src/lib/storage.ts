import { createHash, randomUUID } from "crypto";
import { promises as fs } from "fs";
import path from "path";

const allowedMimeTypes = new Map([
  ["image/jpeg", "jpg"],
  ["image/png", "png"]
]);

export class UploadValidationError extends Error {
  constructor(
    public readonly code: string,
    message: string,
    public readonly status = 400
  ) {
    super(message);
  }
}

export type SavedImageFile = {
  id: string;
  storageKey: string;
  originalName: string;
  mimeType: string;
  size: number;
};

export async function saveUploadedImage(ownerEmail: string, file: File): Promise<SavedImageFile> {
  const maxBytes = Number(process.env.MAX_UPLOAD_BYTES ?? "5242880");
  const extension = allowedMimeTypes.get(file.type);

  if (!extension) {
    throw new UploadValidationError(
      "UNSUPPORTED_IMAGE_TYPE",
      "File gambar harus bertipe image/jpeg atau image/png."
    );
  }

  if (file.size > maxBytes) {
    throw new UploadValidationError(
      "IMAGE_TOO_LARGE",
      `Ukuran gambar maksimal ${maxBytes} byte.`
    );
  }

  const id = randomUUID();
  const ownerFolder = createHash("sha256").update(ownerEmail).digest("hex").slice(0, 24);
  const storageKey = path.posix.join(ownerFolder, `${id}.${extension}`);
  const targetPath = resolveStoragePath(storageKey);
  const bytes = Buffer.from(await file.arrayBuffer());

  await fs.mkdir(path.dirname(targetPath), { recursive: true });
  await fs.writeFile(targetPath, bytes);

  return {
    id,
    storageKey,
    originalName: file.name || `${id}.${extension}`,
    mimeType: file.type,
    size: file.size
  };
}

export async function readImageFile(storageKey: string) {
  return fs.readFile(resolveStoragePath(storageKey));
}

export async function deleteImageFile(storageKey: string) {
  try {
    await fs.unlink(resolveStoragePath(storageKey));
  } catch {
  }
}

function resolveStoragePath(storageKey: string) {
  const uploadRoot = path.resolve(process.cwd(), process.env.UPLOAD_DIR ?? "./uploads");
  const resolved = path.resolve(uploadRoot, ...storageKey.split("/"));

  if (!resolved.startsWith(uploadRoot)) {
    throw new UploadValidationError("INVALID_STORAGE_KEY", "Storage key tidak valid.");
  }

  return resolved;
}
