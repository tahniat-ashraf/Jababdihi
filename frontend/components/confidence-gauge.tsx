"use client";

import { useId } from "react";
import { cn } from "@/lib/utils";

type ConfidenceGaugeProps = {
  score?: number | null;
  label?: string | null;
  fallbackLabel?: string;
  compact?: boolean;
};

const CX = 50;
const CY = 50;
const R = 38;
const ARC = `M ${CX - R},${CY} A ${R},${R} 0 0,1 ${CX + R},${CY}`;

/**
 * Refined Dossier half-speedometer.
 * - Single arc, no segments. Color follows the band:
 *     0–39 vermilion (severe), 40–69 amber, 70–100 deep green.
 * - Score rendered in Newsreader serif numerals beneath the arc.
 * - Pure inherited color for the active arc — themed via `text-*` on parent
 *   is supported by passing a `currentColor` stroke; we use band-specific
 *   stops instead so the gauge stays legible regardless of context color.
 */
export function ConfidenceGauge({
  score,
  label,
  fallbackLabel = "Confidence",
  compact = false
}: ConfidenceGaugeProps) {
  const gId = useId();
  const normalizedScore =
    typeof score === "number" ? Math.max(0, Math.min(100, score)) : null;

  const angleDeg =
    normalizedScore === null ? 90 : 180 - normalizedScore * 1.8;
  const angleRad = (angleDeg * Math.PI) / 180;
  const nx = +(CX + R * Math.cos(angleRad)).toFixed(2);
  const ny = +(CY - R * Math.sin(angleRad)).toFixed(2);

  const activeColor =
    normalizedScore === null
      ? "hsl(var(--faint))"
      : normalizedScore >= 70
        ? "#15803d"
        : normalizedScore >= 40
          ? "#ca8a04"
          : "hsl(var(--severe))";

  const svgWidth = compact ? 78 : 116;

  return (
    <div className="flex flex-col items-center">
      <svg
        aria-label={label ?? fallbackLabel}
        viewBox="0 0 100 56"
        width={svgWidth}
      >
        <defs>
          <linearGradient
            gradientUnits="objectBoundingBox"
            id={gId}
            x1="0"
            x2="1"
            y1="0"
            y2="0"
          >
            <stop offset="0%" stopColor={activeColor} stopOpacity="0.55" />
            <stop offset="100%" stopColor={activeColor} />
          </linearGradient>
        </defs>

        {/* background track — paper rule */}
        <path
          d={ARC}
          fill="none"
          stroke="hsl(var(--border))"
          strokeLinecap="round"
          strokeWidth="6"
        />

        {/* active arc */}
        {normalizedScore !== null && (
          <path
            d={ARC}
            fill="none"
            stroke={`url(#${gId})`}
            strokeLinecap="round"
            strokeWidth="6"
            strokeDasharray={`${(normalizedScore / 100) * 119.4} 200`}
          />
        )}

        {/* needle */}
        {normalizedScore !== null && (
          <line
            stroke="hsl(var(--foreground))"
            strokeLinecap="round"
            strokeWidth="1.4"
            x1={CX}
            x2={nx}
            y1={CY}
            y2={ny}
          />
        )}

        {normalizedScore !== null && (
          <circle cx={CX} cy={CY} fill="hsl(var(--foreground))" r="2.4" />
        )}
      </svg>

      <div className="-mt-1 text-center">
        <div
          className={cn(
            "font-serif font-medium leading-none tracking-tight text-foreground",
            compact ? "text-lg" : "text-3xl"
          )}
        >
          {normalizedScore ?? "—"}
        </div>
        <div
          className="mt-1 truncate text-[10px] uppercase tracking-[0.12em] text-muted-foreground"
          style={{ maxWidth: svgWidth }}
        >
          {label ?? fallbackLabel}
        </div>
      </div>
    </div>
  );
}
