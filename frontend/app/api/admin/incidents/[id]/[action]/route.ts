import { proxyAdminApi } from "@/lib/api-proxy";

export const dynamic = "force-dynamic";

const ALLOWED_ACTIONS = new Set(["publish", "reject", "archive", "reprocess"]);

export async function POST(
  request: Request,
  { params }: { params: Promise<{ id: string; action: string }> }
) {
  const { id, action } = await params;
  if (!ALLOWED_ACTIONS.has(action)) {
    return Response.json({ message: "Unknown action" }, { status: 400 });
  }
  const body = await request.text();
  return proxyAdminApi(
    `/internal/admin/incidents/${id}/${action}`,
    new URLSearchParams(),
    { method: "POST", body: body || undefined }
  );
}
