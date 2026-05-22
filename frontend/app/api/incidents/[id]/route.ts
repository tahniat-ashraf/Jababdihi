import { proxyPublicApi } from "@/lib/api-proxy";

export const dynamic = "force-dynamic";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> }
) {
  const { id } = await params;
  const url = new URL(request.url);

  return proxyPublicApi(`/api/incidents/${id}`, url.searchParams);
}
