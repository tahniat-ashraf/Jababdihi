import { proxyPublicApi } from "@/lib/api-proxy";

export const dynamic = "force-dynamic";

export async function GET(request: Request) {
  const url = new URL(request.url);

  return proxyPublicApi("/api/actors", url.searchParams);
}
