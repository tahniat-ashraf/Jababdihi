import Link from "next/link";
import { ExternalLink } from "lucide-react";
import { ConfidenceGauge } from "@/components/confidence-gauge";
import type { IncidentSummary, LanguageCode } from "@/lib/public-api";
import { cn } from "@/lib/utils";

type IncidentCardProps = {
  incident: IncidentSummary;
  language: LanguageCode;
};

const copy = {
  bn: {
    sourceFallback: "উৎস",
    more: "আরও",
    dateFallback: "তারিখ নেই"
  },
  en: {
    sourceFallback: "Source",
    more: "more",
    dateFallback: "No date"
  }
};

export function IncidentCard({ incident, language }: IncidentCardProps) {
  const labels = copy[language];
  const actorColor =
    incident.actorRole === "OPPOSITION" ? "border-l-red-600" : "border-l-blue-600";
  const location = incident.location?.displayText || incident.location?.district;

  return (
    <article
      className={cn(
        "rounded-md border border-l-4 bg-card p-4 text-card-foreground shadow-sm",
        actorColor
      )}
    >
      <div className="grid gap-5 md:grid-cols-[1fr_auto]">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
            <span>{incident.incidentDate || labels.dateFallback}</span>
            {location ? <span>{location}</span> : null}
            <span
              className={cn(
                "rounded-full px-2 py-0.5 text-white",
                incident.actorRole === "OPPOSITION" ? "bg-red-600" : "bg-blue-600"
              )}
            >
              {incident.actorRole}
            </span>
          </div>

          <h2 className="mt-3 text-lg font-semibold leading-7">
            <Link
              className="hover:underline"
              href={incident.detailUrl}
            >
              {incident.title}
            </Link>
          </h2>

          <p className="mt-2 text-sm leading-6 text-muted-foreground">
            {incident.summary}
          </p>

          <div className="mt-4 flex flex-wrap gap-2">
            {incident.categories.map((category) => (
              <span
                className="rounded-full border bg-background px-2.5 py-1 text-xs font-medium"
                key={category}
              >
                {category}
              </span>
            ))}
          </div>

          <div className="mt-4 flex flex-wrap gap-2">
            {incident.sourcesPreview.map((source, index) => (
              <a
                className="inline-flex max-w-48 items-center gap-1 rounded-full border bg-background px-2.5 py-1 text-xs font-medium transition-colors hover:bg-muted"
                href={source.url}
                key={`${source.url}-${index}`}
                rel="noopener noreferrer"
                target="_blank"
              >
                <span className="truncate">
                  {source.publisherName || labels.sourceFallback}
                </span>
                <ExternalLink className="h-3 w-3 shrink-0" aria-hidden="true" />
              </a>
            ))}
            {incident.remainingSourceCount > 0 ? (
              <span className="rounded-full border bg-muted px-2.5 py-1 text-xs font-medium text-muted-foreground">
                +{incident.remainingSourceCount} {labels.more}
              </span>
            ) : null}
          </div>
        </div>

        <ConfidenceGauge
          label={incident.confidence.label}
          score={incident.confidence.score}
        />
      </div>
    </article>
  );
}
