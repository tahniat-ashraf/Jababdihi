import type { Category, LanguageCode } from "@/lib/public-api";
import { cn } from "@/lib/utils";

type CategorySidebarProps = {
  categories: Category[];
  language: LanguageCode;
  selectedCategories: string[];
  onSelectionChange: (categories: string[]) => void;
};

const copy = {
  bn: {
    title: "ধরন",
    all: "সব",
    none: "কোনোটিই নয়"
  },
  en: {
    title: "Categories",
    all: "All",
    none: "None"
  }
};

export function CategorySidebar({
  categories,
  language,
  selectedCategories,
  onSelectionChange
}: CategorySidebarProps) {
  const selected = new Set(selectedCategories);
  const labels = copy[language];

  function toggleCategory(code: string) {
    if (selected.has(code)) {
      onSelectionChange(selectedCategories.filter((category) => category !== code));
      return;
    }

    onSelectionChange([...selectedCategories, code]);
  }

  return (
    <aside className="lg:sticky lg:top-24 lg:self-start">
      <div className="flex items-center justify-between gap-3 border-b pb-3">
        <h2 className="text-sm font-semibold">{labels.title}</h2>
        <div className="flex items-center gap-2 text-xs">
          <button
            className="text-muted-foreground transition-colors hover:text-foreground"
            type="button"
            onClick={() => onSelectionChange(categories.map((category) => category.code))}
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
      <div className="mt-3 grid gap-1 sm:grid-cols-2 lg:grid-cols-1">
        {categories.map((category) => (
          <label
            className={cn(
              "flex min-h-10 cursor-pointer items-center gap-3 rounded-md border px-3 py-2 text-sm transition-colors",
              selected.has(category.code)
                ? "border-slate-900 bg-slate-50 text-foreground"
                : "border-border text-muted-foreground hover:bg-muted"
            )}
            key={category.code}
          >
            <input
              checked={selected.has(category.code)}
              className="h-4 w-4 rounded border-input accent-slate-900"
              onChange={() => toggleCategory(category.code)}
              type="checkbox"
            />
            <span className="leading-5">{category.label}</span>
          </label>
        ))}
      </div>
    </aside>
  );
}
