import type { Metadata } from "next";
import type { ReactNode } from "react";
import { Suspense } from "react";
import { Newsreader, Noto_Serif_Bengali, Inter_Tight } from "next/font/google";
import "./globals.css";
import { TopNavigation } from "@/components/top-navigation";

const serif = Newsreader({
  subsets: ["latin"],
  display: "swap",
  weight: ["300", "400", "500", "600"],
  style: ["normal", "italic"],
  variable: "--font-serif"
});

const serifBn = Noto_Serif_Bengali({
  subsets: ["bengali"],
  display: "swap",
  weight: ["400", "500", "600"],
  variable: "--font-serif-bn"
});

const sans = Inter_Tight({
  subsets: ["latin"],
  display: "swap",
  weight: ["400", "500", "600", "700"],
  variable: "--font-sans"
});

export const metadata: Metadata = {
  title: "Jababdihi · জবাবদিহি",
  description:
    "Public, evidence-first political accountability platform tracking source-corroborated incidents in Bangladesh."
};

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html
      lang="bn"
      className={`${serif.variable} ${serifBn.variable} ${sans.variable}`}
    >
      <body className="min-h-screen bg-background text-foreground">
        <Suspense fallback={null}>
          <TopNavigation />
        </Suspense>
        <main className="mx-auto flex w-full max-w-3xl flex-1 flex-col px-4 pb-16 pt-3 sm:px-6 lg:max-w-5xl lg:px-8">
          {children}
        </main>
      </body>
    </html>
  );
}
