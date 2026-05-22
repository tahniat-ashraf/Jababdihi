package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RawContentRepository extends JpaRepository<RawContent, UUID> {
  boolean existsByPublisherAndCanonicalUrl(Publisher publisher, String canonicalUrl);

  boolean existsByContentHash(String contentHash);
}
