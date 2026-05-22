import { NextRequest, NextResponse } from "next/server";

export function middleware(request: NextRequest) {
  // In production both env vars MUST be set.  Falling back to a weak default
  // would silently leave the admin page unprotected if a deploy forgets them.
  const isProduction = process.env.NODE_ENV === "production";
  const expectedUsername = process.env.ADMIN_USERNAME;
  const expectedPassword = process.env.ADMIN_PASSWORD;

  if (isProduction && (!expectedUsername || !expectedPassword)) {
    console.error(
      "[middleware] ADMIN_USERNAME / ADMIN_PASSWORD are not set in this production deployment. " +
        "Refusing to serve /admin to avoid exposure with default credentials."
    );
    return new NextResponse("Service Unavailable — admin credentials not configured", {
      status: 503,
    });
  }

  const authorization = request.headers.get("authorization");
  if (!authorization?.startsWith("Basic ")) {
    return unauthorized();
  }
  try {
    const credentials = atob(authorization.slice(6));
    const colon = credentials.indexOf(":");
    const username = credentials.slice(0, colon);
    const password = credentials.slice(colon + 1);
    // Fall back to "admin" only outside production (local dev convenience)
    if (
      username !== (expectedUsername ?? "admin") ||
      password !== (expectedPassword ?? "admin")
    ) {
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
