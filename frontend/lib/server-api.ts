import type { IncidentDetail } from "@/lib/public-api";

const backendBaseUrl =
  process.env.BACKEND_API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  "http://127.0.0.1:8080";

export async function fetchIncidentDetail(
  id: string,
  language: string
): Promise<IncidentDetail | null> {
  const url = new URL(`/api/incidents/${id}`, backendBaseUrl);
  url.searchParams.set("language", language);

  const response = await fetch(url.toString(), {
    cache: "no-store",
    headers: { accept: "application/json" }
  });

  if (response.status === 404) return null;
  if (!response.ok) throw new Error(`Backend error ${response.status}`);

  return response.json() as Promise<IncidentDetail>;
}
