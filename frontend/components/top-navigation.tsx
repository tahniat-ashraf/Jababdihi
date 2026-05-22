"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { BrandMark } from "@/components/brand-mark";
import { cn } from "@/lib/utils";

type ActorRole = "GOVERNMENT" | "OPPOSITION";
type LanguageCode = "bn" | "en";

const actorOptions: Record<
  LanguageCode,
  Array<{ label: string; value: ActorRole }>
> = {
  bn: [
    { label: "সরকার", value: "GOVERNMENT" },
    { label: "বিরোধী দল", value: "OPPOSITION" }
  ],
  en: [
    { label: "Government", value: "GOVERNMENT" },
    { label: "Opposition", value: "OPPOSITION" }
  ]
};

const languageOptions: Array<{
  label: string;
  href: `/${LanguageCode}`;
  code: LanguageCode;
}> = [
  { label: "বাং", href: "/bn", code: "bn" },
  { label: "EN", href: "/en", code: "en" }
];

const wordmarkSubtitleByLang: Record<LanguageCode, string> = {
  bn: "Public · evidence-first",
  en: "জবাবদিহি"
};

export function TopNavigation() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const language: LanguageCode = pathname?.startsWith("/en") ? "en" : "bn";
  const currentActor: ActorRole =
    searchParams.get("actorRole") === "OPPOSITION"
      ? "OPPOSITION"
      : "GOVERNMENT";

  function hrefForActor(actorRole: ActorRole) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("actorRole", actorRole);
    return `${pathname || "/bn"}?${params.toString()}`;
  }

  function hrefForLanguage(href: string) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("actorRole", currentActor);
    return `${href}?${params.toString()}`;
  }

  return (
    <header className="bg-background">
      <div className="mx-auto w-full max-w-3xl px-4 pt-4 sm:px-6 lg:max-w-5xl lg:px-8">
        {/* Masthead row */}
        <div className="flex items-center justify-between gap-3">
          <Link
            href={`/${language}?actorRole=${currentActor}`}
            className="flex items-center gap-2"
          >
            <BrandMark size={26} className="text-foreground" />
            <span className="font-serif text-[1.4rem] font-medium leading-none tracking-masthead text-foreground">
              Jababdihi
            </span>
            <span
              aria-hidden
              className="hidden font-serif text-xs italic text-muted-foreground sm:inline"
            >
              {wordmarkSubtitleByLang[language]}
            </span>
          </Link>

          <nav className="flex items-center gap-3">
            <div
              aria-label="Language"
              className="flex items-center gap-1 text-[11px] font-medium tracking-wide text-muted-foreground"
            >
              {languageOptions.map((option, i) => {
                const active = option.code === language;
                return (
                  <span key={option.code} className="flex items-center gap-1">
                    {i > 0 ? <span className="text-faint">·</span> : null}
                    <Link
                      href={hrefForLanguage(option.href)}
                      className={cn(
                        "tabular-nums transition-colors hover:text-foreground",
                        active
                          ? "font-semibold text-foreground"
                          : "text-muted-foreground"
                      )}
                    >
                      {option.label}
                    </Link>
                  </span>
                );
              })}
            </div>
            <Link
              href="/admin"
              className="text-[11px] font-medium uppercase tracking-[0.14em] text-muted-foreground transition-colors hover:text-foreground"
            >
              Admin
            </Link>
          </nav>
        </div>

        {/* Masthead double rule */}
        <div className="mt-3 h-px bg-foreground/85" />
        <div className="mt-[2px] h-[3px] border-t border-foreground" />

        {/* Actor tabs */}
        <div
          aria-label="Actor role"
          className="flex border-b border-border"
        >
          {actorOptions[language].map((option) => {
            const active = currentActor === option.value;
            const isGov = option.value === "GOVERNMENT";
            return (
              <Link
                key={option.value}
                href={hrefForActor(option.value)}
                aria-current={active ? "page" : undefined}
                className={cn(
                  "group relative flex-1 px-3 py-3 text-center text-sm transition-colors sm:flex-none sm:px-6",
                  active
                    ? "text-foreground"
                    : "text-muted-foreground hover:text-foreground-soft"
                )}
              >
                <span className="inline-flex items-center justify-center gap-2">
                  <span
                    className={cn(
                      "h-1.5 w-1.5 rounded-[1px]",
                      isGov ? "bg-actor-gov" : "bg-actor-opp",
                      !active && "opacity-50"
                    )}
                    aria-hidden
                  />
                  <span className={cn(active && "font-semibold")}>
                    {option.label}
                  </span>
                </span>
                {active ? (
                  <span
                    aria-hidden
                    className={cn(
                      "absolute inset-x-0 -bottom-px h-[2px]",
                      isGov ? "bg-actor-gov" : "bg-actor-opp"
                    )}
                  />
                ) : null}
              </Link>
            );
          })}
        </div>
      </div>
    </header>
  );
}
