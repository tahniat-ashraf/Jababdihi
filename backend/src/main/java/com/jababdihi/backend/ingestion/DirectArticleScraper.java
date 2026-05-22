package com.jababdihi.backend.ingestion;

import java.util.Optional;

interface DirectArticleScraper {
  Optional<ScrapedArticle> scrape(
      String canonicalUrl, IngestionProperties.PublisherFeedProperties publisher);
}
