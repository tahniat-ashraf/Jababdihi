import Link from "next/link";
import { ArrowLeft, ExternalLink } from "lucide-react";
import { ConfidenceGauge } from "@/components/confidence-gauge";
import {
  formatIncidentDate,
  localizeActorRole,
  localizeCategory,
  localizeConfidence,
  localizePublisher,
  UI_COPY
} from "@/lib/i18n";
import type { IncidentDetail, LanguageCode } from "@/lib/public-api";
import { cn } from "@/lib/utils";

type Props = {
  incident: IncidentDetail;
  language: LanguageCode;
};

const copy = {
  bn: {
    back: "ফিডে ফিরে যান",
    categories: "ধরন",
    sources: "উৎস",
    sourceFallback: "উৎস",
    noPublishDate: "তারিখ অজানা"
  },
  en: {
    back: "Back to feed",
    categories: "Categories",
    sources: "Sources",
    sourceFallback: "Source",
    noPublishDate: "Date unknown"
  }
};

function formatSourceDate(
  dateStr: string | null | undefined,
  language: LanguageCode
): string | null {
  if (!dateStr) return null;
  try {
    return new Intl.DateTimeFormat(language === "bn" ? "bn-BD" : "en-GB", {
      year: "numeric",
      month: "long",
      day: "numeric",
      timeZone: "UTC"
    }).format(new Date(dateStr));
  } catch {
    return dateStr;
  }
}

export function IncidentDetail({ incident, language }: Props) {
  const labels = copy[language];
  const isGov = incident.actorRole === "GOVERNMENT";
  const accentBorder = isGov ? "border-l-blue-600" : "border-l-red-600";
  const accentBadge = isGov ? "bg-blue-600" : "bg-red-600";
  const backHref = `/${language}?actorRole=${incident.actorRole}`;
  const location =
    incident.location?.displayText ?? incident.location?.district ?? null;
  const incidentDateFormatted = formatIncidentDate(incident.incidentDate, language);
  const actorLabel = localizeActorRole(incident.actorRole, language);
  const confidenceLabel = localizeConfidence(incident.confidence.label, language);

  return (
    <div className="mx-auto w-full max-w-3xl">
      <Link
        className="mb-6 inline-flex items-center gap-1.5 text-sm text-muted-foreground transition-colors hover:text-foreground"
        href={backHref}
      >
        <ArrowLeft className="h-4 w-4" aria-hidden="true" />
        {labels.back}
      </Link>

      <article
        className={cn(
          "rounded-md border border-l-4 bg-card p-6 shadow-sm",
          accentBorder
        )}
      >
        <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <span className={cn("rounded-full px-2 py-0.5 text-white", accentBadge)}>
            {actorLabel}
          </span>
          {incidentDateFormatted ? <span>{incidentDateFormatted}</span> : null}
          {location ? <span>{location}</span> : null}
        </div>

        <h1 className="mt-4 text-2xl font-semibold leading-8">{incident.title}</h1>

        <p className="mt-4 leading-7 text-muted-foreground">{incident.summary}</p>

        <div className="mt-6 flex flex-col gap-4 sm:flex-row sm:items-start">
          <ConfidenceGauge
            score={incident.confidence.score}
            label={confidenceLabel}
            fallbackLabel={UI_COPY[language].confidence}
          />
          {incident.confidence.explanation ? (
            <p className="text-sm leading-6 text-muted-foreground sm:mt-4">
              {incident.confidence.explanation}
            </p>
          ) : null}
        </div>

        {incident.categories.length > 0 ? (
          <div className="mt-6">
            <h2 className="mb-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {labels.categories}
            </h2>
            <div className="flex flex-wrap gap-2">
              {incident.categories.map((cat) => (
                <span
                  className="rounded-full border bg-background px-2.5 py-1 text-xs font-medium"
                  key={cat}
                >
                  {localizeCategory(cat, language)}
                </span>
              ))}
            </div>
          </div>
        ) : null}

        {incident.sources.length > 0 ? (
          <div className="mt-6">
            <h2 className="mb-3 text-xs font-semibold uppercase tracking-wider text-muted-foreground">
              {labels.sources}
            </h2>
            <ul className="grid gap-2">
              {incident.sources.map((source, i) => (
                <li key={`${source.url}-${i}`}>
                  <a
                    className="group flex items-start justify-between gap-3 rounded-md border bg-background p-3 transition-colors hover:bg-muted"
                    href={source.url}
                    rel="noopener noreferrer"
                    target="_blank"
                  >
                    <div className="min-w-0">
                      <p className="truncate text-sm font-medium group-hover:underline">
                        {source.sourceTitle ||
                          localizePublisher(source.publisherName, language) ||
                          labels.sourceFallback}
                      </p>
                      <p className="mt-0.5 text-xs text-muted-foreground">
                        {localizePublisher(source.publisherName, language) ||
                          labels.sourceFallback}
                        {" · "}
                        {formatSourceDate(source.publishedAt, language) ??
                          labels.noPublishDate}
                      </p>
                    </div>
                    <ExternalLink
                      className="mt-0.5 h-4 w-4 shrink-0 text-muted-foreground"
                      aria-hidden="true"
                    />
                  </a>
                </li>
              ))}
            </ul>
          </div>
        ) : null}

        {incident.disclaimer ? (
          <p className="mt-6 rounded-md border bg-muted/50 px-4 py-3 text-xs leading-5 text-muted-foreground">
            {incident.disclaimer}
          </p>
        ) : null}
      </article>
    </div>
  );
}
