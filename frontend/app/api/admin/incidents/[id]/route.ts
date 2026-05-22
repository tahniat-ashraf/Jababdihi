import { proxyAdminApi } from "@/lib/api-proxy";

export const dynamic = "force-dynamic";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const url = new URL(request.url);
  return proxyAdminApi(`/internal/admin/incidents/${id}`, url.searchParams);
}
