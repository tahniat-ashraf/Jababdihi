import { NextResponse } from "next/server";

const backendBaseUrl =
  process.env.BACKEND_API_BASE_URL ||
  process.env.NEXT_PUBLIC_API_BASE_URL ||
  "http://127.0.0.1:8080";

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
