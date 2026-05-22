import type { Metadata } from "next";
import type { ReactNode } from "react";
import { Suspense } from "react";
import "./globals.css";
import { TopNavigation } from "@/components/top-navigation";

export const metadata: Metadata = {
  title: "Jababdihi",
  description: "Evidence-first political accountability platform"
};

export default function RootLayout({
  children
}: Readonly<{
  children: ReactNode;
}>) {
  return (
    <html lang="bn">
      <body>
        <Suspense fallback={null}>
          <TopNavigation />
        </Suspense>
        <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col px-3 py-4 sm:px-4 lg:px-6">
          {children}
        </main>
      </body>
    </html>
  );
}
