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
import { hasSevereCategory, isSevereCategory } from "@/lib/severity";
import type { IncidentSummary, LanguageCode } from "@/lib/public-api";
import { cn } from "@/lib/utils";

type IncidentCardProps = {
  incident: IncidentSummary;
  language: LanguageCode;
};

const copy = {
  bn: { sourceFallback: "উৎস", more: "আরও", publishers: "প্রকাশক" },
  en: { sourceFallback: "Source", more: "more", publishers: "publishers" }
};

/**
 * Stable, deterministic letter avatar for a publisher source. Hue is derived
 * from the name so the same publisher always gets the same tint.
 */
function PublisherAvatar({ name }: { name: string }) {
  const letters = name
    .split(" ")
    .map((w) => w[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();
  let h = 0;
  for (const ch of name) h = (h * 31 + ch.charCodeAt(0)) % 360;
  return (
    <span
      aria-hidden
      className="inline-flex h-4 w-4 items-center justify-center rounded-[3px] text-[9px] font-bold tracking-tight"
      style={{
        background: `oklch(0.88 0.04 ${h})`,
        color: `oklch(0.30 0.06 ${h})`
      }}
    >
      {letters}
    </span>
  );
}

function SeverityGlyph({ className }: { className?: string }) {
  return (
    <svg
      aria-hidden
      viewBox="0 0 10 8"
      className={cn("inline-block fill-severe align-middle", className)}
      width="10"
      height="8"
    >
      <path d="M5 0 L10 8 L0 8 Z" />
    </svg>
  );
}

export function IncidentCard({ incident, language }: IncidentCardProps) {
  const labels = copy[language];
  const isGov = incident.actorRole === "GOVERNMENT";
  const severe = hasSevereCategory(incident.categories);
  const location =
    incident.location?.displayText || incident.location?.district;
  const date = formatIncidentDate(incident.incidentDate, language);
  const actorLabel = localizeActorRole(incident.actorRole, language);
  const confidenceLabel = localizeConfidence(
    incident.confidence.label,
    language
  );

  const publisherSet = new Set(
    incident.sourcesPreview
      .map((s) => s.publisherName)
      .filter((p): p is string => Boolean(p))
  );
  const publisherCount =
    publisherSet.size + Math.max(0, incident.remainingSourceCount);

  return (
    <article
      className={cn(
        "group relative border-t border-border bg-background px-1 py-5 sm:px-2",
        "first:border-t-0"
      )}
    >
      {severe ? (
        <span
          aria-hidden
          className="absolute inset-y-0 left-0 w-[3px] bg-severe"
        />
      ) : null}

      {/* eyebrow */}
      <div className="flex flex-wrap items-center gap-x-2.5 gap-y-1 text-xs">
        <span
          className={cn(
            "inline-flex items-center gap-1.5 font-semibold uppercase tracking-[0.14em]",
            "text-[10.5px]",
            isGov ? "text-actor-gov" : "text-actor-opp"
          )}
        >
          <span
            aria-hidden
            className={cn(
              "h-1 w-1 rounded-[1px]",
              isGov ? "bg-actor-gov" : "bg-actor-opp"
            )}
          />
          {actorLabel}
        </span>
        {date ? (
          <>
            <span aria-hidden className="h-[3px] w-[3px] rounded-full bg-faint" />
            <span className="text-[11px] text-muted-foreground">{date}</span>
          </>
        ) : null}
        {location ? (
          <>
            <span aria-hidden className="h-[3px] w-[3px] rounded-full bg-faint" />
            <span className="text-[11px] text-muted-foreground">{location}</span>
          </>
        ) : null}
      </div>

      {/* title */}
      <h2 className="mt-2 font-serif text-[1.15rem] font-medium leading-snug tracking-[-0.012em] text-foreground">
        {severe ? <SeverityGlyph className="mr-2 -mt-0.5" /> : null}
        <Link className="hover:underline" href={incident.detailUrl}>
          {incident.title}
        </Link>
      </h2>

      {/* summary */}
      <p className="mt-2 line-clamp-2 font-serif text-[0.875rem] leading-relaxed text-foreground-soft">
        {incident.summary}
      </p>

      {/* categories */}
      {incident.categories.length > 0 ? (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {incident.categories.map((category) => {
            const sev = isSevereCategory(category);
            return (
              <span
                key={category}
                className={cn(
                  "rounded-sm border px-2 py-0.5 text-[10px] font-semibold uppercase tracking-[0.04em]",
                  sev
                    ? "border-severe-soft bg-severe-soft text-severe"
                    : "border-border bg-transparent text-muted-foreground"
                )}
              >
                {localizeCategory(category, language)}
              </span>
            );
          })}
        </div>
      ) : null}

      {/* footer */}
      <div className="mt-3 flex items-end justify-between gap-3 border-t border-dashed border-border-soft pt-3">
        <div className="min-w-0 flex-1">
          <div className="text-[10px] uppercase tracking-[0.12em] text-faint">
            {publisherCount} {labels.publishers}
          </div>
          <div className="mt-1.5 flex flex-wrap items-center gap-x-3 gap-y-1.5">
            {incident.sourcesPreview.slice(0, 3).map((source, index) => {
              const name =
                localizePublisher(source.publisherName, language) ||
                labels.sourceFallback;
              return (
                <a
                  key={`${source.url}-${index}`}
                  href={source.url}
                  rel="noopener noreferrer"
                  target="_blank"
                  className="inline-flex items-center gap-1.5 text-xs text-foreground-soft transition-colors hover:text-foreground"
                >
                  <PublisherAvatar name={source.publisherName ?? name} />
                  <span className="max-w-32 truncate">{name}</span>
                  <ExternalLink
                    className="h-2.5 w-2.5 text-faint"
                    aria-hidden
                  />
                </a>
              );
            })}
            {incident.remainingSourceCount > 0 ? (
              <Link
                href={incident.detailUrl}
                className="text-xs italic text-muted-foreground hover:text-foreground"
              >
                +{incident.remainingSourceCount} {labels.more}
              </Link>
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
