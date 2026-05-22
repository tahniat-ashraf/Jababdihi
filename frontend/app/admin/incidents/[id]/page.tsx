import Link from "next/link";
import { notFound } from "next/navigation";
import type { Metadata } from "next";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { AdminActions } from "@/components/admin-actions";
import { fetchAdminIncident } from "@/lib/admin-server-api";
import { cn } from "@/lib/utils";

type Props = { params: Promise<{ id: string }> };

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { id } = await params;
  const incident = await fetchAdminIncident(id).catch(() => null);
  if (!incident) return {};
  return { title: `[Admin] ${incident.title} — Jababdihi` };
}

function formatInstant(value: string | null | undefined): string {
  if (!value) return "—";
  try {
    return new Intl.DateTimeFormat("en-GB", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      timeZone: "UTC"
    }).format(new Date(value));
  } catch {
    return value;
  }
}

export default async function AdminIncidentPage({ params }: Props) {
  const { id } = await params;
  const incident = await fetchAdminIncident(id);
  if (!incident) notFound();

  const isGov = incident.actorRole === "GOVERNMENT";
  const accentBorder = isGov ? "border-l-blue-600" : "border-l-red-600";
  const accentBadge = isGov ? "bg-blue-600" : "bg-red-600";

  return (
    <div className="mx-auto w-full max-w-3xl">
      <Link
        href="/admin"
        className="mb-6 inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        Back to pending list
      </Link>

      <article
        className={cn(
          "rounded-md border border-l-4 bg-card p-6 shadow-sm",
          accentBorder
        )}
      >
        <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <span className={cn("rounded-full px-2 py-0.5 text-white", accentBadge)}>
            {incident.actorRole}
          </span>
          <span className="rounded-full border bg-muted px-2 py-0.5">
            {incident.status}
          </span>
          {incident.incidentDate ? <span>{incident.incidentDate}</span> : null}
          {incident.locationDistrict ? (
            <span>{incident.locationDistrict}</span>
          ) : null}
          <span className="ml-auto text-muted-foreground">
            {incident.sourceCount} source{incident.sourceCount !== 1 ? "s" : ""}{" "}
            · {incident.independentPublisherCount} publisher
            {incident.independentPublisherCount !== 1 ? "s" : ""}
            {incident.confidenceScore !== null
              ? ` · ${incident.confidenceScore}%`
              : ""}
          </span>
        </div>

        <h1 className="mt-4 text-xl font-semibold leading-7">{incident.title}</h1>

        <p className="mt-3 leading-7 text-muted-foreground">{incident.summary}</p>

        {incident.categories.length > 0 ? (
          <div className="mt-4 flex flex-wrap gap-2">
            {incident.categories.map((cat) => (
              <span
                key={cat}
                className="rounded-full border bg-background px-2.5 py-1 text-xs font-medium"
              >
                {cat}
              </span>
            ))}
          </div>
        ) : null}

        <p className="mt-4 text-xs text-muted-foreground">
          Created: {formatInstant(incident.createdAt)}
        </p>

        {incident.sources.length > 0 ? (
          <div className="mt-6">
            <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              Sources ({incident.sources.length})
            </h2>
            <ul className="grid gap-3">
              {incident.sources.map((source) => (
                <li
                  key={source.id}
                  className="rounded-md border bg-background p-3"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <a
                        href={source.sourceUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="inline-flex items-center gap-1 text-sm font-medium hover:underline"
                      >
                        <span className="truncate">
                          {source.sourceTitle ||
                            source.publisherName ||
                            "Source"}
                        </span>
                        <ExternalLink
                          className="h-3 w-3 shrink-0"
                          aria-hidden="true"
                        />
                      </a>
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        {source.publisherName ?? "Unknown publisher"}
                        {source.publishedAt
                          ? ` · ${formatInstant(source.publishedAt)}`
                          : ""}
                        {source.aiRelevanceScore !== null
                          ? ` · relevance ${source.aiRelevanceScore}`
                          : ""}
                      </p>
                    </div>
                  </div>
                  {source.relevantExcerpt ? (
                    <p className="mt-2 text-xs leading-5 text-muted-foreground">
                      &ldquo;{source.relevantExcerpt}&rdquo;
                    </p>
                  ) : null}
                </li>
              ))}
            </ul>
          </div>
        ) : null}

        <AdminActions
          incidentId={incident.id}
          currentStatus={incident.status}
        />
      </article>
    </div>
  );
}
