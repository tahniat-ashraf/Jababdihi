import Link from "next/link";
import { fetchAdminPending } from "@/lib/admin-server-api";
import { cn } from "@/lib/utils";

type Props = { searchParams: Promise<{ page?: string }> };

export const metadata = { title: "Admin — Jababdihi" };

export default async function AdminPage({ searchParams }: Props) {
  const { page: pageParam } = await searchParams;
  const page = Math.max(1, parseInt(pageParam ?? "1", 10) || 1);
  const data = await fetchAdminPending(page, 20);

  return (
    <div className="mx-auto w-full max-w-4xl">
      <div className="mb-6">
        <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">
          Admin
        </p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight">
          Pending incident review
        </h1>
        <p className="mt-1 text-sm text-muted-foreground">
          {data.totalItems} incident{data.totalItems !== 1 ? "s" : ""} awaiting
          review
        </p>
      </div>

      {data.items.length === 0 ? (
        <p className="rounded-md border bg-muted/50 px-4 py-6 text-center text-sm text-muted-foreground">
          No pending incidents.
        </p>
      ) : (
        <ul className="grid gap-3">
          {data.items.map((incident) => (
            <li key={incident.id}>
              <Link
                href={`/admin/incidents/${incident.id}`}
                className="group block rounded-md border bg-card p-4 shadow-sm transition-colors hover:bg-muted/50"
              >
                <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                  <span
                    className={cn(
                      "rounded-full px-2 py-0.5 text-white",
                      incident.actorRole === "GOVERNMENT"
                        ? "bg-blue-600"
                        : "bg-red-600"
                    )}
                  >
                    {incident.actorRole}
                  </span>
                  {incident.incidentDate ? (
                    <span>{incident.incidentDate}</span>
                  ) : null}
                  <span className="ml-auto">
                    {incident.sourceCount} source
                    {incident.sourceCount !== 1 ? "s" : ""}
                    {incident.confidenceScore !== null
                      ? ` · ${incident.confidenceScore}%`
                      : ""}
                  </span>
                </div>
                <p className="mt-2 font-medium leading-6 group-hover:underline">
                  {incident.title}
                </p>
                <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                  {incident.summary}
                </p>
              </Link>
            </li>
          ))}
        </ul>
      )}

      {data.totalPages > 1 ? (
        <div className="mt-6 flex justify-center gap-2 text-sm">
          {page > 1 ? (
            <Link
              href={`/admin?page=${page - 1}`}
              className="rounded-md border px-3 py-1.5 hover:bg-muted"
            >
              Previous
            </Link>
          ) : null}
          <span className="px-3 py-1.5 text-muted-foreground">
            Page {page} of {data.totalPages}
          </span>
          {page < data.totalPages ? (
            <Link
              href={`/admin?page=${page + 1}`}
              className="rounded-md border px-3 py-1.5 hover:bg-muted"
            >
              Next
            </Link>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}
