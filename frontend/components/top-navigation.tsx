"use client";

import Link from "next/link";
import { usePathname, useSearchParams } from "next/navigation";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type ActorRole = "GOVERNMENT" | "OPPOSITION";

const actorOptions: Array<{ label: string; value: ActorRole }> = [
  { label: "Government", value: "GOVERNMENT" },
  { label: "Opposition", value: "OPPOSITION" }
];

const languageOptions = [
  { label: "বাংলা", href: "/bn", code: "bn" },
  { label: "English", href: "/en", code: "en" }
];

export function TopNavigation() {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const currentActor =
    searchParams.get("actorRole") === "OPPOSITION"
      ? "OPPOSITION"
      : "GOVERNMENT";

  function hrefForActor(actorRole: ActorRole) {
    const params = new URLSearchParams(searchParams.toString());
    params.set("actorRole", actorRole);

    return `${pathname || "/bn"}?${params.toString()}`;
  }

  return (
    <header className="border-b bg-background">
      <div className="mx-auto flex min-h-16 w-full max-w-6xl flex-col gap-3 px-4 py-4 sm:px-6 lg:flex-row lg:items-center lg:justify-between lg:px-8">
        <div className="flex items-center justify-between gap-4">
          <Link href="/bn" className="text-lg font-semibold tracking-tight">
            Jababdihi
          </Link>
          <Link
            href="/admin"
            className="text-sm font-medium text-muted-foreground transition-colors hover:text-foreground lg:hidden"
          >
            Admin
          </Link>
        </div>

        <nav className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between lg:justify-end">
          <div
            aria-label="Actor role"
            className="inline-flex w-full rounded-md border bg-muted p-1 sm:w-auto"
          >
            {actorOptions.map((option) => (
              <Button
                asChild
                key={option.value}
                size="sm"
                variant={currentActor === option.value ? "default" : "ghost"}
                className={cn(
                  "flex-1 sm:flex-none",
                  option.value === "GOVERNMENT" &&
                    currentActor === option.value &&
                    "bg-blue-600 hover:bg-blue-700",
                  option.value === "OPPOSITION" &&
                    currentActor === option.value &&
                    "bg-red-600 hover:bg-red-700"
                )}
              >
                <Link href={hrefForActor(option.value)}>{option.label}</Link>
              </Button>
            ))}
          </div>

          <div
            aria-label="Language"
            className="inline-flex w-full rounded-md border bg-muted p-1 sm:w-auto"
          >
            {languageOptions.map((option) => {
              const active = pathname?.startsWith(option.href);

              return (
                <Button
                  asChild
                  key={option.code}
                  size="sm"
                  variant={active ? "secondary" : "ghost"}
                  className="flex-1 sm:flex-none"
                >
                  <Link href={option.href}>{option.label}</Link>
                </Button>
              );
            })}
          </div>

          <Link
            href="/admin"
            className="hidden text-sm font-medium text-muted-foreground transition-colors hover:text-foreground lg:inline-flex"
          >
            Admin
          </Link>
        </nav>
      </div>
    </header>
  );
}
