"use client";

import { useEffect } from "react";
import { X } from "lucide-react";
import type { Category, LanguageCode } from "@/lib/public-api";
import { isSevereCategory } from "@/lib/severity";
import { cn } from "@/lib/utils";

type CategoryFilterProps = {
  categories: Category[];
  language: LanguageCode;
  selectedCategories: string[];
  onSelectionChange: (categories: string[]) => void;
};

const copy = {
  bn: {
    title: "ধরন",
    eyebrow: "ছাঁকুন",
    all: "সব",
    none: "কোনোটিই নয়",
    open: "ছাঁকুন",
    close: "বন্ধ করুন",
    apply: "ফলাফল দেখুন",
    selected: "নির্বাচিত"
  },
  en: {
    title: "Categories",
    eyebrow: "Filter",
    all: "All",
    none: "None",
    open: "Filter",
    close: "Close",
    apply: "Show results",
    selected: "selected"
  }
};

// ─── shared content ─────────────────────────────────────────────────
function FilterBody({
  categories,
  language,
  selectedCategories,
  onSelectionChange
}: CategoryFilterProps) {
  const selected = new Set(selectedCategories);
  const labels = copy[language];

  function toggleCategory(code: string) {
    if (selected.has(code)) {
      onSelectionChange(
        selectedCategories.filter((category) => category !== code)
      );
      return;
    }
    onSelectionChange([...selectedCategories, code]);
  }

  return (
    <>
      <div className="flex items-baseline justify-between gap-3">
        <div>
          <div className="text-[10px] font-medium uppercase tracking-[0.18em] text-muted-foreground">
            {labels.eyebrow}
          </div>
          <h2 className="mt-0.5 font-serif text-lg font-medium tracking-[-0.015em]">
            {labels.title}
          </h2>
        </div>
        <div className="flex items-center gap-3 text-xs">
          <button
            className="font-medium text-foreground underline underline-offset-4 transition-colors hover:text-actor-gov"
            type="button"
            onClick={() =>
              onSelectionChange(categories.map((category) => category.code))
            }
          >
            {labels.all}
          </button>
          <button
            className="text-muted-foreground transition-colors hover:text-foreground"
            type="button"
            onClick={() => onSelectionChange([])}
          >
            {labels.none}
          </button>
        </div>
      </div>

      <ul className="mt-2 grid gap-px">
        {categories.map((category) => {
          const on = selected.has(category.code);
          const sev = isSevereCategory(category.code);
          return (
            <li key={category.code}>
              <label
                className={cn(
                  "flex cursor-pointer items-center justify-between border-b border-border-soft py-3 text-sm transition-colors"
                )}
              >
                <span className="flex items-center gap-3">
                  {sev ? (
                    <span
                      aria-hidden
                      className="inline-block h-0 w-0 border-x-[4px] border-b-[6px] border-x-transparent border-b-severe"
                    />
                  ) : null}
                  <span
                    className={cn(
                      "leading-5",
                      on ? "font-medium text-foreground" : "text-faint"
                    )}
                  >
                    {category.label}
                  </span>
                </span>
                <span
                  aria-hidden
                  className={cn(
                    "ml-3 inline-flex h-[22px] w-[22px] items-center justify-center rounded-[4px] border-[1.5px] transition-colors",
                    on
                      ? "border-foreground bg-foreground text-background"
                      : "border-border bg-transparent"
                  )}
                >
                  {on ? (
                    <svg
                      width="12"
                      height="12"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="3.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <polyline points="20 6 9 17 4 12" />
                    </svg>
                  ) : null}
                </span>
                <input
                  className="sr-only"
                  checked={on}
                  onChange={() => toggleCategory(category.code)}
                  type="checkbox"
                />
              </label>
            </li>
          );
        })}
      </ul>
    </>
  );
}

// ─── desktop sidebar ────────────────────────────────────────────────
export function CategorySidebar(props: CategoryFilterProps) {
  return (
    <aside className="hidden lg:sticky lg:top-6 lg:block lg:self-start">
      <FilterBody {...props} />
    </aside>
  );
}

// ─── mobile bottom sheet ────────────────────────────────────────────
type SheetProps = CategoryFilterProps & {
  open: boolean;
  onClose: () => void;
  totalItems?: number;
};

export function CategoryFilterSheet({
  open,
  onClose,
  totalItems,
  ...props
}: SheetProps) {
  const labels = copy[props.language];

  useEffect(() => {
    if (!open) return;
    const prev = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = prev;
    };
  }, [open]);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      aria-modal="true"
      role="dialog"
      className="fixed inset-0 z-50 flex flex-col lg:hidden"
    >
      <button
        type="button"
        aria-label={labels.close}
        onClick={onClose}
        className="flex-1 bg-foreground/45 backdrop-blur-[1px]"
      />
      <div className="relative max-h-[88vh] overflow-hidden rounded-t-2xl bg-background pb-[max(env(safe-area-inset-bottom),1.25rem)] shadow-[0_-12px_40px_rgba(0,0,0,0.12)]">
        <div className="flex items-center justify-center pb-2 pt-3">
          <div className="h-1 w-9 rounded-full bg-border" />
        </div>
        <div className="absolute right-3 top-3">
          <button
            type="button"
            onClick={onClose}
            aria-label={labels.close}
            className="inline-flex h-8 w-8 items-center justify-center rounded-full text-muted-foreground hover:text-foreground"
          >
            <X className="h-4 w-4" aria-hidden />
          </button>
        </div>
        <div className="overflow-y-auto px-5 pb-2 pt-1" style={{ maxHeight: "70vh" }}>
          <FilterBody {...props} />
        </div>
        <div className="border-t border-border-soft px-5 pt-3">
          <button
            type="button"
            onClick={onClose}
            className="inline-flex w-full items-center justify-center gap-2 rounded-[3px] bg-foreground px-4 py-3.5 text-sm font-semibold tracking-[-0.005em] text-background"
          >
            {labels.apply}
            {typeof totalItems === "number" ? (
              <span className="font-mono text-xs opacity-80">
                ({totalItems.toLocaleString(props.language)})
              </span>
            ) : null}
          </button>
        </div>
      </div>
    </div>
  );
}
