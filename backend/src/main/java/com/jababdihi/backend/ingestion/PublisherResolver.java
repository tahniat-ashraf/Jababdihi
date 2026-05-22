package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import com.jababdihi.backend.source.PublisherRepository;
import com.jababdihi.backend.source.PublisherType;
import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class PublisherResolver {
  private final PublisherRepository publisherRepository;
  private final Clock clock;

  PublisherResolver(PublisherRepository publisherRepository, Clock clock) {
    this.publisherRepository = publisherRepository;
    this.clock = clock;
  }

  Publisher resolve(IngestionProperties.PublisherFeedProperties config) {
    String domain = config.getDomain().toLowerCase(Locale.ROOT);
    return publisherRepository
        .findByDomain(domain)
        .orElseGet(() -> publisherRepository.save(createPublisher(config, domain)));
  }

  private Publisher createPublisher(
      IngestionProperties.PublisherFeedProperties config, String domain) {
    Instant now = clock.instant();
    Publisher publisher = new Publisher();
    publisher.setName(config.getName());
    publisher.setType(PublisherType.NEWSPAPER);
    publisher.setDomain(domain);
    publisher.setHomepageUrl(config.getHomepageUrl());
    publisher.setActive(true);
    publisher.setCreatedAt(now);
    publisher.setUpdatedAt(now);
    return publisher;
  }
}
