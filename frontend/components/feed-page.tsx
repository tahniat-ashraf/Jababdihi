"use client";

import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { CategorySidebar } from "@/components/category-sidebar";
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
    government: "সরকার",
    opposition: "বিরোধী দল",
    loading: "লোড হচ্ছে",
    loadingMore: "আরও লোড হচ্ছে",
    empty: "কোনো প্রকাশিত ঘটনা পাওয়া যায়নি",
    unavailable: "ফিড লোড করা যায়নি"
  },
  en: {
    title: "Accountability feed",
    government: "Government",
    opposition: "Opposition",
    loading: "Loading",
    loadingMore: "Loading more",
    empty: "No published incidents found",
    unavailable: "Could not load the feed"
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
    async (nextPage: number, mode: "replace" | "append", signal?: AbortSignal) => {
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
  }, [actorRole, categoriesLoaded, labels.unavailable, language, loadIncidents, selectedCategoryKey]);

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
  }, [hasMore, isLoading, isLoadingMore, labels.unavailable, loadIncidents, page]);

  const actorLabel = useMemo(
    () => (actorRole === "OPPOSITION" ? labels.opposition : labels.government),
    [actorRole, labels.government, labels.opposition]
  );

  return (
    <div className="grid gap-4 lg:grid-cols-[14rem_1fr]">
      <CategorySidebar
        categories={categories}
        language={language}
        selectedCategories={selectedCategories}
        onSelectionChange={setSelectedCategories}
      />

      <section className="min-w-0">
        <div className="mb-5 flex flex-col gap-2 border-b pb-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">
              {actorLabel}
            </p>
            <h1 className="text-2xl font-semibold tracking-tight">
              {labels.title}
            </h1>
          </div>
          <p className="text-sm text-muted-foreground">
            {totalItems.toLocaleString(language)}
          </p>
        </div>

        {error ? (
          <div className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-700">
            {error}
          </div>
        ) : null}

        {isLoading ? (
          <div className="rounded-md border bg-card p-5 text-sm text-muted-foreground">
            {labels.loading}
          </div>
        ) : null}

        {!isLoading && !error && incidents.length === 0 ? (
          <div className="rounded-md border bg-card p-5 text-sm text-muted-foreground">
            {labels.empty}
          </div>
        ) : null}

        <div className="grid gap-2">
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
          <div className="py-4 text-center text-sm text-muted-foreground">
            {labels.loadingMore}
          </div>
        ) : null}
      </section>
    </div>
  );
}
