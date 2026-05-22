"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { SlidersHorizontal } from "lucide-react";
import {
  CategoryFilterSheet,
  CategorySidebar
} from "@/components/category-sidebar";
import { IncidentCard } from "@/components/incident-card";
import type {
  ActorRole,
  ApiError,
  CategoriesResponse,
  Category,
  IncidentSummary,
  LanguageCode,
  PageResponse
} from "@/lib/public-api";

type FeedPageProps = {
  language: LanguageCode;
};

const pageSize = 20;

const copy = {
  bn: {
    title: "জবাবদিহি ফিড",
    eyebrow: "জবাবদিহিতা ফিড · ১৭ ফেব্রু থেকে আজ",
    government: "সরকার",
    opposition: "বিরোধী দল",
    loading: "লোড হচ্ছে",
    loadingMore: "আরও লোড হচ্ছে",
    empty: "কোনো প্রকাশিত ঘটনা পাওয়া যায়নি",
    unavailable: "ফিড লোড করা যায়নি",
    filters: "ছাঁকুন",
    sort: "সাজান",
    recommended: "সুপারিশকৃত",
    categories: "ধরন",
    footnote:
      "ট্র্যাকিং শুরু ১৭ ফেব্রুয়ারি ২০২৬। আস্থা মানে উৎস-সমর্থনের শক্তি — আইনি সত্য নয়।"
  },
  en: {
    title: "Accountability feed",
    eyebrow: "Accountability feed · 17 Feb → today",
    government: "Government",
    opposition: "Opposition",
    loading: "Loading",
    loadingMore: "Loading more",
    empty: "No published incidents found",
    unavailable: "Could not load the feed",
    filters: "Filters",
    sort: "Sort",
    recommended: "Recommended",
    categories: "categories",
    footnote:
      "Tracking starts 17 February 2026. Confidence reflects source corroboration — not legal truth."
  }
};

function parseActorRole(value: string | null): ActorRole {
  return value === "OPPOSITION" ? "OPPOSITION" : "GOVERNMENT";
}

async function readJson<T>(response: Response): Promise<T> {
  const json = (await response.json()) as unknown;

  if (!response.ok) {
    const message =
      typeof json === "object" &&
      json !== null &&
      "message" in json &&
      typeof (json as ApiError).message === "string"
        ? (json as ApiError).message
        : "Request failed";
    throw new Error(message);
  }

  return json as T;
}

export function FeedPage({ language }: FeedPageProps) {
  const pathname = usePathname();
  const router = useRouter();
  const searchParams = useSearchParams();
  const actorRoleParam = searchParams.get("actorRole");
  const actorRole = parseActorRole(actorRoleParam);
  const labels = copy[language];

  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategories, setSelectedCategories] = useState<string[]>([]);
  const [incidents, setIncidents] = useState<IncidentSummary[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [isLoadingMore, setIsLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [categoriesLoaded, setCategoriesLoaded] = useState(false);
  const [filterSheetOpen, setFilterSheetOpen] = useState(false);
  const loadMoreRef = useRef<HTMLDivElement | null>(null);

  const selectedCategoryKey = selectedCategories.join(",");
  const hasMore = totalPages === 0 ? false : page < totalPages;

  useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());
    if (actorRoleParam !== "GOVERNMENT" && actorRoleParam !== "OPPOSITION") {
      params.set("actorRole", actorRole);
      router.replace(`${pathname}?${params.toString()}`);
    }
  }, [actorRole, actorRoleParam, pathname, router, searchParams]);

  useEffect(() => {
    const controller = new AbortController();

    async function loadCategories() {
      setCategoriesLoaded(false);
      const response = await fetch(`/api/categories?language=${language}`, {
        signal: controller.signal
      });
      const data = await readJson<CategoriesResponse>(response);

      setCategories(data.items);
      setSelectedCategories(data.items.map((category) => category.code));
      setCategoriesLoaded(true);
    }

    loadCategories().catch((categoryError: Error) => {
      if (controller.signal.aborted) {
        return;
      }
      setError(categoryError.message || labels.unavailable);
      setCategoriesLoaded(true);
    });

    return () => controller.abort();
  }, [labels.unavailable, language]);

  const loadIncidents = useCallback(
    async (
      nextPage: number,
      mode: "replace" | "append",
      signal?: AbortSignal
    ) => {
      if (categories.length > 0 && selectedCategories.length === 0) {
        setIncidents([]);
        setPage(1);
        setTotalPages(0);
        setTotalItems(0);
        setIsLoading(false);
        setIsLoadingMore(false);
        return;
      }

      const params = new URLSearchParams({
        actorRole,
        language,
        page: String(nextPage),
        pageSize: String(pageSize),
        sort: "RECOMMENDED"
      });
      selectedCategories.forEach((category) => {
        params.append("category", category);
      });

      if (mode === "replace") {
        setIsLoading(true);
      } else {
        setIsLoadingMore(true);
      }
      setError(null);

      const response = await fetch(`/api/incidents?${params.toString()}`, {
        signal
      });
      const data = await readJson<PageResponse<IncidentSummary>>(response);

      setIncidents((current) =>
        mode === "append" ? [...current, ...data.items] : data.items
      );
      setPage(data.page);
      setTotalPages(data.totalPages);
      setTotalItems(data.totalItems);
      setIsLoading(false);
      setIsLoadingMore(false);
    },
    [actorRole, categories.length, language, selectedCategories]
  );

  useEffect(() => {
    if (!categoriesLoaded) {
      return;
    }

    const controller = new AbortController();
    loadIncidents(1, "replace", controller.signal).catch((feedError: Error) => {
      if (controller.signal.aborted) {
        return;
      }
      setError(feedError.message || labels.unavailable);
      setIsLoading(false);
      setIsLoadingMore(false);
    });

    return () => controller.abort();
  }, [
    actorRole,
    categoriesLoaded,
    labels.unavailable,
    language,
    loadIncidents,
    selectedCategoryKey
  ]);

  useEffect(() => {
    const target = loadMoreRef.current;
    if (!target || !hasMore || isLoading || isLoadingMore) {
      return;
    }

    const observer = new IntersectionObserver(
      (entries) => {
        const [entry] = entries;
        if (!entry.isIntersecting) {
          return;
        }
        loadIncidents(page + 1, "append").catch((feedError: Error) => {
          setError(feedError.message || labels.unavailable);
          setIsLoadingMore(false);
        });
      },
      { rootMargin: "500px 0px" }
    );

    observer.observe(target);
    return () => observer.disconnect();
  }, [
    hasMore,
    isLoading,
    isLoadingMore,
    labels.unavailable,
    loadIncidents,
    page
  ]);

  const totalLocalized = useMemo(
    () => totalItems.toLocaleString(language),
    [language, totalItems]
  );

  return (
    <div className="grid gap-6 pt-5 lg:grid-cols-[15rem_1fr] lg:gap-10">
      <CategorySidebar
        categories={categories}
        language={language}
        selectedCategories={selectedCategories}
        onSelectionChange={setSelectedCategories}
      />

      <section className="min-w-0">
        {/* Eyebrow + title */}
        <header className="pb-4">
          <p className="text-[10px] font-medium uppercase tracking-[0.16em] text-muted-foreground">
            {labels.eyebrow}
          </p>
          <div className="mt-2 flex items-start justify-between gap-3">
            <h1 className="font-serif text-[2rem] font-medium leading-[1.05] tracking-[-0.025em] text-foreground sm:text-[2.5rem]">
              {labels.title}
            </h1>
            <span className="mt-2 shrink-0 font-mono text-[11px] tracking-tight text-muted-foreground">
              {totalLocalized}
            </span>
          </div>
        </header>

        {/* Filter / sort row */}
        <div className="flex items-center justify-between gap-2 border-y border-border py-2">
          <button
            type="button"
            onClick={() => setFilterSheetOpen(true)}
            className="inline-flex items-center gap-2 rounded-full border border-border bg-paper px-3 py-1.5 text-xs font-medium text-foreground transition-colors hover:border-foreground/40 lg:hidden"
          >
            <SlidersHorizontal className="h-3 w-3" aria-hidden />
            {selectedCategories.length === categories.length
              ? `${categories.length} ${labels.categories}`
              : `${selectedCategories.length} / ${categories.length} ${labels.categories}`}
          </button>
          {/* desktop spacer */}
          <span className="hidden text-[11px] text-muted-foreground lg:inline">
            {selectedCategories.length === categories.length
              ? `${categories.length} ${labels.categories}`
              : `${selectedCategories.length} / ${categories.length} ${labels.categories}`}
          </span>

          <div className="text-[11px] text-muted-foreground">
            {labels.sort}:{" "}
            <span className="font-semibold text-foreground">
              {labels.recommended}
            </span>
          </div>
        </div>

        {error ? (
          <div className="mt-4 border-l-2 border-severe bg-severe-soft px-3 py-2 text-sm text-severe">
            {error}
          </div>
        ) : null}

        {isLoading ? (
          <div className="py-10 text-center font-serif text-sm italic text-muted-foreground">
            {labels.loading}…
          </div>
        ) : null}

        {!isLoading && !error && incidents.length === 0 ? (
          <div className="py-10 text-center font-serif text-sm italic text-muted-foreground">
            {labels.empty}
          </div>
        ) : null}

        <div className="flex flex-col">
          {incidents.map((incident) => (
            <IncidentCard
              incident={incident}
              key={incident.id}
              language={language}
            />
          ))}
        </div>

        <div ref={loadMoreRef} className="h-8" />
        {isLoadingMore ? (
          <div className="py-4 text-center text-xs italic text-muted-foreground">
            {labels.loadingMore}…
          </div>
        ) : null}

        {!isLoading && !error && incidents.length > 0 ? (
          <footer className="mt-10 border-t border-border-soft pt-6 text-center font-serif text-xs italic leading-relaxed text-muted-foreground">
            {labels.footnote}
          </footer>
        ) : null}
      </section>

      <CategoryFilterSheet
        open={filterSheetOpen}
        onClose={() => setFilterSheetOpen(false)}
        categories={categories}
        language={language}
        selectedCategories={selectedCategories}
        onSelectionChange={setSelectedCategories}
        totalItems={totalItems}
      />
    </div>
  );
}
