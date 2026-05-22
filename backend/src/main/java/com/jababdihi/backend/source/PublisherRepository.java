package com.jababdihi.backend.source;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PublisherRepository extends JpaRepository<Publisher, UUID> {
  Optional<Publisher> findByDomain(String domain);
}
