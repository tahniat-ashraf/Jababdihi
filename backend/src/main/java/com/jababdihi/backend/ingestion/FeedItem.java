package com.jababdihi.backend.ingestion;

import java.time.Instant;
import java.util.Optional;

record FeedItem(
    String sourceUrl, String title, Optional<Instant> publishedAt, String extractedText) {}
