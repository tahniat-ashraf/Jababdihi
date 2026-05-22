import { proxyAdminApi } from "@/lib/api-proxy";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);
  return proxyAdminApi("/internal/admin/incidents/pending", url.searchParams);
}
