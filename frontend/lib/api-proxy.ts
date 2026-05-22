import { NextResponse } from "next/server";

const backendBaseUrl =
  process.env.BACKEND_API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  "http://127.0.0.1:8080";

const adminApiKey = process.env.ADMIN_API_KEY || "dev-api-key";

export async function proxyPublicApi(
  path: string,
  searchParams: URLSearchParams
) {
  const upstreamUrl = new URL(path, backendBaseUrl);
  searchParams.forEach((value, key) => {
    upstreamUrl.searchParams.append(key, value);
  });

  try {
    const response = await fetch(upstreamUrl, {
      cache: "no-store",
      headers: {
        accept: "application/json"
      }
    });

    const body = await response.text();
    return new NextResponse(body, {
      status: response.status,
      headers: {
        "content-type":
          response.headers.get("content-type") || "application/json"
      }
    });
  } catch {
    return NextResponse.json(
      { message: "Public API is unavailable" },
      { status: 502 }
    );
  }
}

export async function proxyAdminApi(
  path: string,
  searchParams: URLSearchParams,
  options?: { method?: string; body?: string }
) {
  const upstreamUrl = new URL(path, backendBaseUrl);
  const method = options?.method ?? "GET";
  if (method === "GET") {
    searchParams.forEach((value, key) => {
      upstreamUrl.searchParams.append(key, value);
    });
  }

  try {
    const response = await fetch(upstreamUrl, {
      method,
      cache: "no-store",
      headers: {
        accept: "application/json",
        "content-type": "application/json",
        "x-api-key": adminApiKey
      },
      body: options?.body
    });

    const responseBody = await response.text();
    return new NextResponse(responseBody, {
      status: response.status,
      headers: {
        "content-type":
          response.headers.get("content-type") || "application/json"
      }
    });
  } catch {
    return NextResponse.json(
      { message: "Admin API is unavailable" },
      { status: 502 }
    );
  }
}
