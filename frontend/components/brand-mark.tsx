import type { SVGProps } from "react";
import { cn } from "@/lib/utils";

type BrandMarkProps = SVGProps<SVGSVGElement> & {
  size?: number;
  title?: string;
};

const bars = [
  { x: 25, w: 14, o: 0.4 },
  { x: 21, w: 22, o: 0.52 },
  { x: 17, w: 30, o: 0.64 },
  { x: 13, w: 38, o: 0.76 },
  { x: 9, w: 46, o: 0.88 }
];

/**
 * Jababdihi mark — record strata.
 * Five accumulating bars (narrow → wide). Uses `currentColor`, so set color
 * via Tailwind text-* classes on the parent.
 */
export function BrandMark({
  size = 28,
  title = "Jababdihi",
  className,
  ...rest
}: BrandMarkProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 64 64"
      role="img"
      aria-label={title}
      className={cn("inline-block flex-shrink-0", className)}
      {...rest}
    >
      <title>{title}</title>
      {bars.map((b, i) => (
        <rect
          key={i}
          x={b.x}
          y={14 + i * 7.5}
          width={b.w}
          height={4}
          rx="1"
          fill="currentColor"
          opacity={b.o}
        />
      ))}
    </svg>
  );
}
