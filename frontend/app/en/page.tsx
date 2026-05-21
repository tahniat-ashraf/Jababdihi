import { Suspense } from "react";
import { FeedPage } from "@/components/feed-page";

export default function EnglishFeedPage() {
  return (
    <Suspense fallback={null}>
      <FeedPage language="en" />
    </Suspense>
  );
}
