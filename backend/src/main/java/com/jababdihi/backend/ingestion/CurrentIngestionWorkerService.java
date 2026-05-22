package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.incident.processing.IncidentProcessingService;
import com.jababdihi.backend.source.Publisher;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
public class CurrentIngestionWorkerService {
  private static final Duration INITIAL_CURRENT_WINDOW = Duration.ofHours(1);
  private static final Duration MAX_RUNTIME = Duration.ofHours(1);

  private final IngestionProperties ingestionProperties;
  private final PublisherResolver publisherResolver;
  private final IngestionCursorService cursorService;
  private final AllowlistedPublisherIngestionService ingestionService;
  private final IncidentProcessingService incidentProcessingService;
  private final Clock clock;

  CurrentIngestionWorkerService(
      IngestionProperties ingestionProperties,
      PublisherResolver publisherResolver,
      IngestionCursorService cursorService,
      AllowlistedPublisherIngestionService ingestionService,
      IncidentProcessingService incidentProcessingService,
      Clock clock) {
    this.ingestionProperties = ingestionProperties;
    this.publisherResolver = publisherResolver;
    this.cursorService = cursorService;
    this.ingestionService = ingestionService;
    this.incidentProcessingService = incidentProcessingService;
    this.clock = clock;
  }

  public CurrentIngestionRunResult runCurrentIngestion() {
    String workerId = "current-ingestion-" + UUID.randomUUID();
    Instant deadline = Instant.now(clock).plus(MAX_RUNTIME);
    int remainingItems = ingestionProperties.getItemLimit();
    int fetchedItems = 0;
    int attemptedItems = 0;
    int storedItems = 0;
    int skippedItems = 0;
    int processedRawContents = 0;
    int failedPublishers = 0;
    int skippedLockedPublishers = 0;

    for (IngestionProperties.PublisherFeedProperties publisherConfig :
        ingestionProperties.getPublishers()) {
      if (remainingItems == 0) {
        break;
      }
      if (!Instant.now(clock).isBefore(deadline)) {
        break;
      }

      Publisher publisher = publisherResolver.resolve(publisherConfig);
      Instant initialCursor = Instant.now(clock).minus(INITIAL_CURRENT_WINDOW);
      var claimedCursor =
          cursorService.claim(
              IngestionJobType.CURRENT_INGESTION, publisher, workerId, initialCursor);
      if (claimedCursor.isEmpty()) {
        skippedLockedPublishers++;
        continue;
      }

      IngestionCursor cursor = claimedCursor.get();
      try {
        int publisherLimit = remainingItems < 0 ? -1 : remainingItems;
        Instant cursorPublishedAt =
            cursor.getCursorPublishedAt() == null ? initialCursor : cursor.getCursorPublishedAt();
        CurrentPublisherIngestionResult result =
            ingestionService.ingestCurrentPublisher(
                publisher, publisherConfig, cursorPublishedAt, publisherLimit, deadline);
        for (UUID rawContentId : result.rawContentIds()) {
          if (rawContentId != null) {
            incidentProcessingService.processRawContent(rawContentId);
            processedRawContents++;
          }
        }
        fetchedItems += result.fetchedItems();
        attemptedItems += result.attemptedItems();
        storedItems += result.storedItems();
        skippedItems += result.skippedItems();
        if (remainingItems > 0) {
          remainingItems -= result.attemptedItems();
        }
        cursorService.releaseSucceeded(
            cursor, result.cursorPublishedAt().orElse(cursorPublishedAt));
      } catch (RuntimeException ex) {
        failedPublishers++;
        cursorService.releaseFailed(cursor, ex);
      }
    }

    return new CurrentIngestionRunResult(
        fetchedItems,
        attemptedItems,
        storedItems,
        skippedItems,
        processedRawContents,
        failedPublishers,
        skippedLockedPublishers);
  }
}
