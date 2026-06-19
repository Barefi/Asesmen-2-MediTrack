export function getOwnerEmail(request: Request): string | null {
  const value = request.headers.get("authorization")?.trim();
  if (!value) return null;

  const normalized = value.toLowerCase();
  const isEmailLike = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(normalized);
  return isEmailLike ? normalized : null;
}
