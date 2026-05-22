package com.jababdihi.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.incident.processing.IncidentProcessingService;
import com.jababdihi.backend.source.Publisher;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CurrentIngestionWorkerServiceTests {
  private static final Instant NOW = Instant.parse("2026-05-22T12:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final PublisherResolver publisherResolver = mock(PublisherResolver.class);
  private final IngestionCursorService cursorService = mock(IngestionCursorService.class);
  private final AllowlistedPublisherIngestionService ingestionService =
      mock(AllowlistedPublisherIngestionService.class);
  private final IncidentProcessingService incidentProcessingService =
      mock(IncidentProcessingService.class);

  @Test
  void runCurrentIngestionClaimsCursorAppliesLimitAndProcessesStoredRawContent() {
    Publisher publisher = publisher();
    IngestionCursor cursor = cursor(publisher, Instant.parse("2026-05-22T11:00:00Z"));
    UUID rawContentId = UUID.randomUUID();
    IngestionProperties properties = properties(10);
    CurrentIngestionWorkerService service = service(properties);

    when(publisherResolver.resolve(any())).thenReturn(publisher);
    when(cursorService.claim(
            eq(IngestionJobType.CURRENT_INGESTION),
            eq(publisher),
            any(),
            eq(Instant.parse("2026-05-22T11:00:00Z"))))
        .thenReturn(Optional.of(cursor));
    when(ingestionService.ingestCurrentPublisher(
            eq(publisher), any(), eq(cursor.getCursorPublishedAt()), eq(10), any()))
        .thenReturn(
            new CurrentPublisherIngestionResult(
                2,
                2,
                1,
                1,
                Optional.of(Instant.parse("2026-05-22T11:30:00Z")),
                List.of(rawContentId)));

    CurrentIngestionRunResult result = service.runCurrentIngestion();

    assertThat(result.attemptedItems()).isEqualTo(2);
    assertThat(result.storedItems()).isEqualTo(1);
    assertThat(result.processedRawContents()).isEqualTo(1);
    verify(incidentProcessingService).processRawContent(rawContentId);
    verify(cursorService).releaseSucceeded(cursor, Instant.parse("2026-05-22T11:30:00Z"));
  }

  @Test
  void runCurrentIngestionSkipsLockedPublisher() {
    Publisher publisher = publisher();
    IngestionProperties properties = properties(10);
    CurrentIngestionWorkerService service = service(properties);

    when(publisherResolver.resolve(any())).thenReturn(publisher);
    when(cursorService.claim(eq(IngestionJobType.CURRENT_INGESTION), eq(publisher), any(), any()))
        .thenReturn(Optional.empty());

    CurrentIngestionRunResult result = service.runCurrentIngestion();

    assertThat(result.skippedLockedPublishers()).isEqualTo(1);
    verify(ingestionService, never()).ingestCurrentPublisher(any(), any(), any(), anyInt(), any());
  }

  private CurrentIngestionWorkerService service(IngestionProperties properties) {
    return new CurrentIngestionWorkerService(
        properties,
        publisherResolver,
        cursorService,
        ingestionService,
        incidentProcessingService,
        CLOCK);
  }

  private IngestionProperties properties(int itemLimit) {
    IngestionProperties properties = new IngestionProperties();
    properties.setItemLimit(itemLimit);
    properties.setPublishers(List.of(publisherConfig()));
    return properties;
  }

  private IngestionProperties.PublisherFeedProperties publisherConfig() {
    IngestionProperties.PublisherFeedProperties publisher =
        new IngestionProperties.PublisherFeedProperties();
    publisher.setName("Prothom Alo");
    publisher.setDomain("prothomalo.com");
    publisher.setHomepageUrl("https://www.prothomalo.com");
    publisher.setFeedUrl("https://www.prothomalo.com/feed");
    publisher.setLanguageCode("bn");
    return publisher;
  }

  private Publisher publisher() {
    Publisher publisher = new Publisher();
    publisher.setName("Prothom Alo");
    publisher.setDomain("prothomalo.com");
    return publisher;
  }

  private IngestionCursor cursor(Publisher publisher, Instant cursorPublishedAt) {
    IngestionCursor cursor = new IngestionCursor();
    cursor.setJobType(IngestionJobType.CURRENT_INGESTION);
    cursor.setPublisher(publisher);
    cursor.setCursorPublishedAt(cursorPublishedAt);
    return cursor;
  }
}
