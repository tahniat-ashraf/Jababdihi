import { cn } from "@/lib/utils";

type ConfidenceGaugeProps = {
  score?: number | null;
  label?: string | null;
};

export function ConfidenceGauge({ score, label }: ConfidenceGaugeProps) {
  const normalizedScore =
    typeof score === "number" ? Math.max(0, Math.min(100, score)) : null;
  const rotation =
    normalizedScore === null ? -90 : Math.round(normalizedScore * 1.8 - 90);

  return (
    <div className="flex min-w-32 flex-col items-center gap-1">
      <div
        aria-label={label || "Confidence"}
        className="relative h-16 w-32 overflow-hidden"
      >
        <div className="absolute left-0 top-0 h-32 w-32 rounded-full border-[10px] border-slate-200" />
        <div
          className={cn(
            "absolute left-0 top-0 h-32 w-32 rounded-full border-[10px]",
            normalizedScore === null
              ? "border-slate-300"
              : "border-b-blue-600 border-l-blue-600 border-r-red-500 border-t-red-500"
          )}
        />
        <div className="absolute bottom-0 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full bg-slate-900" />
        <div
          className="absolute bottom-0 left-1/2 h-1 w-12 origin-left rounded-full bg-slate-900 transition-transform"
          style={{ transform: `rotate(${rotation}deg)` }}
        />
      </div>
      <div className="text-center">
        <div className="text-xl font-semibold leading-none">
          {normalizedScore ?? "--"}
        </div>
        <div className="mt-1 max-w-32 truncate text-xs text-muted-foreground">
          {label || "Confidence"}
        </div>
      </div>
    </div>
  );
}
