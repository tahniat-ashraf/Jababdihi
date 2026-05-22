package com.jababdihi.backend.ingestion;

import java.time.Instant;
import java.util.Optional;

record ScrapedArticle(String title, Optional<Instant> publishedAt, String extractedText) {}
