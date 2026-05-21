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
        <main className="mx-auto flex w-full max-w-6xl flex-1 flex-col px-4 py-8 sm:px-6 lg:px-8">
          {children}
        </main>
      </body>
    </html>
  );
}
