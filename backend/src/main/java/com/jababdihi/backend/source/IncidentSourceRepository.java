package com.jababdihi.backend.source;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentSourceRepository extends JpaRepository<IncidentSource, UUID> {
  boolean existsByCanonicalUrl(String canonicalUrl);

  Optional<IncidentSource> findByCanonicalUrl(String canonicalUrl);
}
