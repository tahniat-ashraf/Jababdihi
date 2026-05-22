import { NextRequest, NextResponse } from "next/server";

export function middleware(request: NextRequest) {
  const authorization = request.headers.get("authorization");
  if (!authorization?.startsWith("Basic ")) {
    return unauthorized();
  }
  try {
    const credentials = atob(authorization.slice(6));
    const colon = credentials.indexOf(":");
    const username = credentials.slice(0, colon);
    const password = credentials.slice(colon + 1);
    const expectedUsername = process.env.ADMIN_USERNAME || "admin";
    const expectedPassword = process.env.ADMIN_PASSWORD || "admin";
    if (username !== expectedUsername || password !== expectedPassword) {
      return unauthorized();
    }
  } catch {
    return unauthorized();
  }
  return NextResponse.next();
}

function unauthorized() {
  return new NextResponse("Unauthorized", {
    status: 401,
    headers: { "WWW-Authenticate": 'Basic realm="Jababdihi Admin"' }
  });
}

export const config = {
  matcher: ["/admin/:path*"]
};
