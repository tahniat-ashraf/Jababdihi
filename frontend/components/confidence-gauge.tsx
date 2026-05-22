import { cn } from "@/lib/utils";

type ConfidenceGaugeProps = {
  score?: number | null;
  label?: string | null;
  fallbackLabel?: string;
  compact?: boolean;
};

export function ConfidenceGauge({
  score,
  label,
  fallbackLabel = "Confidence",
  compact = false
}: ConfidenceGaugeProps) {
  const normalizedScore =
    typeof score === "number" ? Math.max(0, Math.min(100, score)) : null;
  const rotation =
    normalizedScore === null ? -90 : Math.round(normalizedScore * 1.8 - 90);

  const dialSize = compact
    ? "h-10 w-20 [--r:theme(spacing.20)]"
    : "h-16 w-32 [--r:theme(spacing.32)]";
  const fullSize = compact ? "h-20 w-20" : "h-32 w-32";
  const border = compact ? "border-[7px]" : "border-[10px]";
  const needleLen = compact ? "w-9" : "w-12";
  const scoreText = compact ? "text-base" : "text-xl";
  const minW = compact ? "min-w-20" : "min-w-32";

  return (
    <div className={cn("flex flex-col items-center gap-0.5", minW)}>
      <div
        aria-label={label ?? fallbackLabel}
        className={cn("relative overflow-hidden", dialSize)}
      >
        <div
          className={cn(
            "absolute left-0 top-0 rounded-full border-slate-200",
            fullSize,
            border
          )}
        />
        <div
          className={cn(
            "absolute left-0 top-0 rounded-full",
            fullSize,
            border,
            normalizedScore === null
              ? "border-slate-300"
              : "border-b-blue-600 border-l-blue-600 border-r-red-500 border-t-red-500"
          )}
        />
        <div className="absolute bottom-0 left-1/2 h-1 w-1 -translate-x-1/2 rounded-full bg-slate-900" />
        <div
          className={cn(
            "absolute bottom-0 left-1/2 h-1 origin-left rounded-full bg-slate-900 transition-transform",
            needleLen
          )}
          style={{ transform: `rotate(${rotation}deg)` }}
        />
      </div>
      <div className="text-center">
        <div className={cn("font-semibold leading-none", scoreText)}>
          {normalizedScore ?? "--"}
        </div>
        <div className="mt-0.5 max-w-20 truncate text-xs text-muted-foreground">
          {label ?? fallbackLabel}
        </div>
      </div>
    </div>
  );
}
