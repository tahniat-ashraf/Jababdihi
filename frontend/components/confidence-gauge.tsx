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

  const svgWidth = compact ? 68 : 104;

  return (
    <div className="flex flex-col items-center">
      <svg
        aria-label={label ?? fallbackLabel}
        viewBox="0 0 100 52"
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
            <stop offset="0%" stopColor="#b91c1c" />
            <stop offset="30%" stopColor="#f97316" />
            <stop offset="55%" stopColor="#eab308" />
            <stop offset="78%" stopColor="#84cc16" />
            <stop offset="100%" stopColor="#15803d" />
          </linearGradient>
        </defs>

        {/* background track */}
        <path
          d={ARC}
          fill="none"
          stroke="#e2e8f0"
          strokeLinecap="round"
          strokeWidth="9"
        />

        {/* gradient track */}
        {normalizedScore !== null && (
          <path
            d={ARC}
            fill="none"
            stroke={`url(#${gId})`}
            strokeLinecap="round"
            strokeWidth="9"
          />
        )}

        {/* needle */}
        {normalizedScore !== null && (
          <line
            stroke="#0f172a"
            strokeLinecap="round"
            strokeWidth="2.2"
            x1={CX}
            x2={nx}
            y1={CY}
            y2={ny}
          />
        )}

        {/* pivot dot */}
        {normalizedScore !== null && (
          <circle cx={CX} cy={CY} fill="#0f172a" r="2.8" />
        )}
      </svg>

      <div className="-mt-0.5 text-center">
        <div
          className={cn(
            "font-semibold leading-none",
            compact ? "text-sm" : "text-xl"
          )}
        >
          {normalizedScore ?? "--"}
        </div>
        <div
          className="mt-0.5 truncate text-xs text-muted-foreground"
          style={{ maxWidth: svgWidth }}
        >
          {label ?? fallbackLabel}
        </div>
      </div>
    </div>
  );
}
