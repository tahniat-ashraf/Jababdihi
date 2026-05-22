package com.jababdihi.backend.ingestion;

import java.util.List;

interface FeedClient {
  List<FeedItem> fetch(IngestionProperties.PublisherFeedProperties publisher);
}
