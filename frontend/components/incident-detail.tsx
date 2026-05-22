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
import { hasSevereCategory, isSevereCategory } from "@/lib/severity";
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
    summary: "সারসংক্ষেপ",
    sources: "উৎস",
    sourceFallback: "উৎস",
    noPublishDate: "তারিখ অজানা",
    openInNewTab: "নতুন ট্যাবে খুলবে",
    sourcesCount: "উৎস"
  },
  en: {
    back: "Feed",
    categories: "Categories",
    summary: "Summary",
    sources: "Sources",
    sourceFallback: "Source",
    noPublishDate: "Date unknown",
    openInNewTab: "opens in a new tab",
    sourcesCount: "sources"
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
      month: "short",
      day: "numeric",
      timeZone: "UTC"
    }).format(new Date(dateStr));
  } catch {
    return dateStr;
  }
}

function SeverityGlyph({ size = 12 }: { size?: number }) {
  return (
    <svg
      aria-hidden
      viewBox="0 0 10 8"
      width={size}
      height={(size * 8) / 10}
      className="inline-block fill-severe align-middle"
    >
      <path d="M5 0 L10 8 L0 8 Z" />
    </svg>
  );
}

export function IncidentDetail({ incident, language }: Props) {
  const labels = copy[language];
  const isGov = incident.actorRole === "GOVERNMENT";
  const severe = hasSevereCategory(incident.categories);
  const backHref = `/${language}?actorRole=${incident.actorRole}`;
  const location =
    incident.location?.displayText ?? incident.location?.district ?? null;
  const incidentDateFormatted = formatIncidentDate(
    incident.incidentDate,
    language
  );
  const actorLabel = localizeActorRole(incident.actorRole, language);
  const confidenceLabel = localizeConfidence(
    incident.confidence.label,
    language
  );

  return (
    <div className="mx-auto w-full max-w-2xl">
      <div className="flex items-center justify-between pb-2 pt-1">
        <Link
          className="inline-flex items-center gap-1.5 text-sm text-foreground-soft transition-colors hover:text-foreground"
          href={backHref}
        >
          <ArrowLeft className="h-3.5 w-3.5" aria-hidden />
          {labels.back}
        </Link>
        <span className="font-mono text-[11px] uppercase tracking-[0.12em] text-muted-foreground">
          #{incident.id.slice(0, 8)}
        </span>
      </div>

      <div className="h-px bg-foreground/85" />
      <div className="mt-[2px] h-[3px] border-t border-foreground" />

      <article className="pt-5">
        <div className="flex flex-wrap items-center gap-x-2.5 gap-y-1 text-xs">
          <span
            className={cn(
              "inline-flex items-center gap-1.5 text-[10.5px] font-semibold uppercase tracking-[0.14em]",
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
          {incidentDateFormatted ? (
            <>
              <span aria-hidden className="h-[3px] w-[3px] rounded-full bg-faint" />
              <span className="text-[11px] text-muted-foreground">
                {incidentDateFormatted}
              </span>
            </>
          ) : null}
          {location ? (
            <>
              <span aria-hidden className="h-[3px] w-[3px] rounded-full bg-faint" />
              <span className="text-[11px] text-muted-foreground">{location}</span>
            </>
          ) : null}
        </div>

        <h1 className="mt-3 font-serif text-[1.75rem] font-medium leading-[1.1] tracking-[-0.025em] text-foreground sm:text-[2rem]">
          {severe ? <SeverityGlyph size={14} /> : null}
          {severe ? " " : null}
          {incident.title}
        </h1>

        {/* Confidence row */}
        <div className="mt-5 flex items-center gap-5 border border-border bg-paper px-4 py-4">
          <ConfidenceGauge
            score={incident.confidence.score}
            label={confidenceLabel}
            fallbackLabel={UI_COPY[language].confidence}
          />
          {incident.confidence.explanation ? (
            <p className="font-serif text-[0.875rem] italic leading-relaxed text-foreground-soft">
              {incident.confidence.explanation}
            </p>
          ) : null}
        </div>

        {/* Summary */}
        <section className="mt-6">
          <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
            {labels.summary}
          </h2>
          <p className="mt-2 font-serif text-[1rem] leading-relaxed text-foreground">
            {incident.summary}
          </p>
        </section>

        {/* Categories */}
        {incident.categories.length > 0 ? (
          <section className="mt-6">
            <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
              {labels.categories}
            </h2>
            <div className="mt-2 flex flex-wrap gap-1.5">
              {incident.categories.map((cat) => {
                const sev = isSevereCategory(cat);
                return (
                  <span
                    className={cn(
                      "rounded-sm border px-2 py-0.5 text-[11px] font-semibold uppercase tracking-[0.03em]",
                      sev
                        ? "border-severe-soft bg-severe-soft text-severe"
                        : "border-border bg-paper text-foreground-soft"
                    )}
                    key={cat}
                  >
                    {localizeCategory(cat, language)}
                  </span>
                );
              })}
            </div>
          </section>
        ) : null}

        {/* Sources */}
        {incident.sources.length > 0 ? (
          <section className="mt-6">
            <div className="flex items-baseline justify-between">
              <h2 className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
                {labels.sources} · {incident.sources.length}
              </h2>
              <span className="font-serif text-[11px] italic text-muted-foreground">
                {labels.openInNewTab} ↗
              </span>
            </div>

            <ul className="mt-2 border-t border-border">
              {incident.sources.map((source, i) => (
                <li
                  key={`${source.url}-${i}`}
                  className="border-b border-border-soft"
                >
                  <a
                    className="group flex items-center justify-between gap-3 py-3 text-sm transition-colors"
                    href={source.url}
                    rel="noopener noreferrer"
                    target="_blank"
                  >
                    <div className="min-w-0">
                      <p className="font-serif text-[0.95rem] leading-snug text-foreground group-hover:underline">
                        {source.sourceTitle ||
                          localizePublisher(source.publisherName, language) ||
                          labels.sourceFallback}
                      </p>
                      <p className="mt-0.5 text-[11px] text-muted-foreground">
                        {localizePublisher(source.publisherName, language) ||
                          labels.sourceFallback}
                        {" · "}
                        {formatSourceDate(source.publishedAt, language) ??
                          labels.noPublishDate}
                      </p>
                    </div>
                    <ExternalLink
                      className="h-3.5 w-3.5 shrink-0 text-faint"
                      aria-hidden
                    />
                  </a>
                </li>
              ))}
            </ul>
          </section>
        ) : null}

        {/* Disclaimer */}
        {incident.disclaimer ? (
          <aside className="mt-6 border-l-2 border-foreground bg-[hsl(40_45%_90%)] px-4 py-3">
            <div className="text-[10px] font-bold uppercase tracking-[0.14em] text-foreground">
              {language === "bn"
                ? "এর অর্থ কী"
                : "A note on what this means"}
            </div>
            <p className="mt-1 font-serif text-[0.8125rem] italic leading-relaxed text-foreground-soft">
              {incident.disclaimer}
            </p>
          </aside>
        ) : null}
      </article>
    </div>
  );
}
