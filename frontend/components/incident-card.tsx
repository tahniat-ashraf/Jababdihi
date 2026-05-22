import Link from "next/link";
import { ExternalLink } from "lucide-react";
import { ConfidenceGauge } from "@/components/confidence-gauge";
import {
  formatIncidentDate,
  localizeActorRole,
  localizeCategory,
  localizeConfidence,
  localizePublisher,
  UI_COPY
} from "@/lib/i18n";
import type { IncidentSummary, LanguageCode } from "@/lib/public-api";
import { cn } from "@/lib/utils";

type IncidentCardProps = {
  incident: IncidentSummary;
  language: LanguageCode;
};

const copy = {
  bn: { sourceFallback: "উৎস", more: "আরও" },
  en: { sourceFallback: "Source", more: "more" }
};

export function IncidentCard({ incident, language }: IncidentCardProps) {
  const labels = copy[language];
  const isGov = incident.actorRole === "GOVERNMENT";
  const accentBorder = isGov ? "border-l-blue-600" : "border-l-red-600";
  const accentBadge = isGov ? "bg-blue-600" : "bg-red-600";
  const location =
    incident.location?.displayText || incident.location?.district;
  const date = formatIncidentDate(incident.incidentDate, language);
  const actorLabel = localizeActorRole(incident.actorRole, language);
  const confidenceLabel = localizeConfidence(
    incident.confidence.label,
    language
  );

  return (
    <article
      className={cn(
        "rounded-md border border-l-4 bg-card p-3 text-card-foreground shadow-sm",
        accentBorder
      )}
    >
      <div className="flex items-start gap-3">
        <div className="min-w-0 flex-1">
          <div className="flex flex-wrap items-center gap-1.5 text-xs text-muted-foreground">
            <span
              className={cn(
                "rounded-full px-2 py-0.5 text-white",
                accentBadge
              )}
            >
              {actorLabel}
            </span>
            {date ? <span>{date}</span> : null}
            {location ? <span>{location}</span> : null}
          </div>

          <h2 className="mt-1.5 text-[0.9375rem] font-semibold leading-6">
            <Link className="hover:underline" href={incident.detailUrl}>
              {incident.title}
            </Link>
          </h2>

          <p className="mt-1 line-clamp-1 text-sm leading-5 text-muted-foreground">
            {incident.summary}
          </p>

          <div className="mt-2 flex flex-wrap gap-1">
            {incident.categories.map((category) => (
              <span
                className="rounded-full border bg-background px-2 py-0.5 text-xs font-medium"
                key={category}
              >
                {localizeCategory(category, language)}
              </span>
            ))}
            {incident.sourcesPreview.map((source, index) => (
              <a
                className="inline-flex max-w-40 items-center gap-1 rounded-full border bg-background px-2 py-0.5 text-xs font-medium transition-colors hover:bg-muted"
                href={source.url}
                key={`${source.url}-${index}`}
                rel="noopener noreferrer"
                target="_blank"
              >
                <span className="truncate">
                  {localizePublisher(source.publisherName, language) ||
                    labels.sourceFallback}
                </span>
                <ExternalLink
                  className="h-2.5 w-2.5 shrink-0"
                  aria-hidden="true"
                />
              </a>
            ))}
            {incident.remainingSourceCount > 0 ? (
              <span className="rounded-full border bg-muted px-2 py-0.5 text-xs font-medium text-muted-foreground">
                +{incident.remainingSourceCount} {labels.more}
              </span>
            ) : null}
          </div>
        </div>

        <ConfidenceGauge
          compact
          label={confidenceLabel}
          fallbackLabel={UI_COPY[language].confidence}
          score={incident.confidence.score}
        />
      </div>
    </article>
  );
}
