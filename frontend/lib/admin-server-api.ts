import type {
  AdminIncidentDetail,
  AdminPendingIncident,
  PageResponse
} from "@/lib/admin-api";

const backendBaseUrl =
  process.env.BACKEND_API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  "http://127.0.0.1:8080";

const adminApiKey = process.env.ADMIN_API_KEY || "dev-api-key";

async function adminFetch(url: URL): Promise<Response> {
  return fetch(url.toString(), {
    cache: "no-store",
    headers: { accept: "application/json", "x-api-key": adminApiKey }
  });
}

export async function fetchAdminPending(
  page = 1,
  pageSize = 20
): Promise<PageResponse<AdminPendingIncident>> {
  const url = new URL("/internal/admin/incidents/pending", backendBaseUrl);
  url.searchParams.set("page", String(page));
  url.searchParams.set("pageSize", String(pageSize));
  const response = await adminFetch(url);
  if (!response.ok) throw new Error(`Admin API error ${response.status}`);
  return response.json() as Promise<PageResponse<AdminPendingIncident>>;
}

export async function fetchAdminIncident(
  id: string
): Promise<AdminIncidentDetail | null> {
  const url = new URL(`/internal/admin/incidents/${id}`, backendBaseUrl);
  const response = await adminFetch(url);
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(`Admin API error ${response.status}`);
  return response.json() as Promise<AdminIncidentDetail>;
}
