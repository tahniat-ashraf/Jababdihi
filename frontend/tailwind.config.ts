import type { Config } from "tailwindcss";
import tailwindcssAnimate from "tailwindcss-animate";

const config: Config = {
  darkMode: ["class"],
  content: [
    "./app/**/*.{ts,tsx}",
    "./components/**/*.{ts,tsx}",
    "./lib/**/*.{ts,tsx}"
  ],
  theme: {
    extend: {
      colors: {
        border: "hsl(var(--border))",
        "border-soft": "hsl(var(--border-soft))",
        input: "hsl(var(--input))",
        ring: "hsl(var(--ring))",
        background: "hsl(var(--background))",
        paper: "hsl(var(--paper))",
        foreground: "hsl(var(--foreground))",
        "foreground-soft": "hsl(var(--foreground-soft))",
        faint: "hsl(var(--faint))",
        primary: {
          DEFAULT: "hsl(var(--primary))",
          foreground: "hsl(var(--primary-foreground))"
        },
        secondary: {
          DEFAULT: "hsl(var(--secondary))",
          foreground: "hsl(var(--secondary-foreground))"
        },
        muted: {
          DEFAULT: "hsl(var(--muted))",
          foreground: "hsl(var(--muted-foreground))"
        },
        accent: {
          DEFAULT: "hsl(var(--accent))",
          foreground: "hsl(var(--accent-foreground))"
        },
        destructive: {
          DEFAULT: "hsl(var(--destructive))",
          foreground: "hsl(var(--destructive-foreground))"
        },
        card: {
          DEFAULT: "hsl(var(--card))",
          foreground: "hsl(var(--card-foreground))"
        },
        "actor-gov": {
          DEFAULT: "hsl(var(--actor-gov))",
          soft: "hsl(var(--actor-gov-soft))"
        },
        "actor-opp": {
          DEFAULT: "hsl(var(--actor-opp))",
          soft: "hsl(var(--actor-opp-soft))"
        },
        severe: {
          DEFAULT: "hsl(var(--severe))",
          soft: "hsl(var(--severe-soft))"
        }
      },
      fontFamily: {
        sans: [
          "var(--font-sans)",
          "Inter Tight",
          "system-ui",
          "sans-serif"
        ],
        serif: [
          "var(--font-serif)",
          "var(--font-serif-bn)",
          "Newsreader",
          "Noto Serif Bengali",
          "Georgia",
          "serif"
        ],
        mono: ["ui-monospace", "SFMono-Regular", "Menlo", "monospace"]
      },
      borderRadius: {
        lg: "var(--radius)",
        md: "calc(var(--radius) - 2px)",
        sm: "calc(var(--radius) - 4px)"
      },
      letterSpacing: {
        masthead: "-0.025em"
      }
    }
  },
  plugins: [tailwindcssAnimate]
};

export default config;
