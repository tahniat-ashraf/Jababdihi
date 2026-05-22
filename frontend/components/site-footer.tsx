"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { UI_COPY } from "@/lib/i18n";
import type { LanguageCode } from "@/lib/public-api";

export function SiteFooter() {
  const pathname = usePathname();
  const language: LanguageCode = pathname?.startsWith("/en") ? "en" : "bn";
  const copy = UI_COPY[language];

  return (
    <footer className="mx-auto w-full max-w-3xl px-4 pb-8 sm:px-6 lg:max-w-5xl lg:px-8">
      <div className="border-t border-border-soft pt-4 font-serif text-xs italic leading-relaxed text-muted-foreground">
        <p>{copy.footerDisclaimer}</p>
        <div className="mt-2 flex gap-3 font-sans text-[11px] not-italic uppercase tracking-[0.12em]">
          <Link href={`/${language}/methodology`} className="hover:text-foreground">
            {copy.methodology}
          </Link>
          <span aria-hidden className="text-faint">
            ·
          </span>
          <Link href={`/${language}/methodology#legal`} className="hover:text-foreground">
            {copy.legal}
          </Link>
        </div>
      </div>
    </footer>
  );
}
