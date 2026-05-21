import { Suspense } from "react";
import { FeedPage } from "@/components/feed-page";

export default function BanglaFeedPage() {
  return (
    <Suspense fallback={null}>
      <FeedPage language="bn" />
    </Suspense>
  );
}
