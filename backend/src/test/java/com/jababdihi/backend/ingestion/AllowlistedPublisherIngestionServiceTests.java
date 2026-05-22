package com.jababdihi.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.observability.OperationalMetricsService;
import com.jababdihi.backend.source.Publisher;
import com.jababdihi.backend.source.PublisherRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AllowlistedPublisherIngestionServiceTests {
  private static final Instant NOW = Instant.parse("2026-05-22T12:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final RawContentRepository rawContentRepository = mock(RawContentRepository.class);
  private final PublisherRepository publisherRepository = mock(PublisherRepository.class);
  private final PublisherResolver publisherResolver =
      new PublisherResolver(publisherRepository, CLOCK);
  private final OperationalMetricsService metricsService = mock(OperationalMetricsService.class);

  @Test
  void ingestPublisherStoresAllowlistedRssItemWithCanonicalUrlAndHash() {
    Publisher publisher = publisher();
    when(publisherRepository.findByDomain("prothomalo.com")).thenReturn(Optional.of(publisher));
    FeedClient feedClient =
        ignored ->
            List.of(
                new FeedItem(
                    "https://www.prothomalo.com/bangladesh/story/?utm_source=rss&fbclid=1",
                    "Story title",
                    Optional.of(Instant.parse("2026-05-22T09:00:00Z")),
                    "Clean extracted text"));
    DirectArticleScraper articleScraper = mock(DirectArticleScraper.class);
    AllowlistedPublisherIngestionService service = service(feedClient, articleScraper);

    IngestionResult result = service.ingestPublisher(publisherConfig());

    assertThat(result).isEqualTo(new IngestionResult(1, 1, 0));
    ArgumentCaptor<RawContent> rawContentCaptor = ArgumentCaptor.forClass(RawContent.class);
    verify(rawContentRepository).save(rawContentCaptor.capture());
    RawContent rawContent = rawContentCaptor.getValue();
    assertThat(rawContent.getPublisher()).isEqualTo(publisher);
    assertThat(rawContent.getSourceUrl()).contains("utm_source=rss");
    assertThat(rawContent.getCanonicalUrl())
        .isEqualTo("https://www.prothomalo.com/bangladesh/story");
    assertThat(rawContent.getSourceTitle()).isEqualTo("Story title");
    assertThat(rawContent.getPublishedAt()).isEqualTo(Instant.parse("2026-05-22T09:00:00Z"));
    assertThat(rawContent.getExtractedText()).isEqualTo("Clean extracted text");
    assertThat(rawContent.getRelevantExcerpt()).isEmpty();
    assertThat(rawContent.getContentHash()).hasSize(64);
    assertThat(rawContent.getLanguageCode()).isEqualTo("bn");
    assertThat(rawContent.getFetchedAt()).isEqualTo(NOW);
    assertThat(rawContent.getParsingStatus()).isEqualTo("PARSED");
    assertThat(rawContent.getAiProcessingStatus()).isEqualTo("PENDING");
    verify(articleScraper, never()).scrape(any(), any());
  }

  @Test
  void ingestPublisherUsesScrapeFallbackWhenFeedHasNoText() {
    Publisher publisher = publisher();
    when(publisherRepository.findByDomain("prothomalo.com")).thenReturn(Optional.of(publisher));
    FeedClient feedClient =
        ignored ->
            List.of(
                new FeedItem(
                    "https://www.prothomalo.com/bangladesh/story",
                    "RSS title",
                    Optional.empty(),
                    ""));
    DirectArticleScraper articleScraper = mock(DirectArticleScraper.class);
    when(articleScraper.scrape(eq("https://www.prothomalo.com/bangladesh/story"), any()))
        .thenReturn(
            Optional.of(new ScrapedArticle("Scraped title", Optional.empty(), "Article text")));
    AllowlistedPublisherIngestionService service = service(feedClient, articleScraper);

    IngestionResult result = service.ingestPublisher(publisherConfig());

    assertThat(result).isEqualTo(new IngestionResult(1, 1, 0));
    ArgumentCaptor<RawContent> rawContentCaptor = ArgumentCaptor.forClass(RawContent.class);
    verify(rawContentRepository).save(rawContentCaptor.capture());
    assertThat(rawContentCaptor.getValue().getSourceTitle()).isEqualTo("Scraped title");
    assertThat(rawContentCaptor.getValue().getExtractedText()).isEqualTo("Article text");
  }

  @Test
  void ingestPublisherSkipsUnknownDomainsAndDuplicates() {
    Publisher publisher = publisher();
    when(publisherRepository.findByDomain("prothomalo.com")).thenReturn(Optional.of(publisher));
    FeedClient feedClient =
        ignored ->
            List.of(
                new FeedItem("https://facebook.com/share/1", "Social", Optional.empty(), "text"),
                new FeedItem(
                    "https://www.prothomalo.com/bangladesh/story",
                    "Duplicate",
                    Optional.empty(),
                    "text"));
    when(rawContentRepository.existsByPublisherAndCanonicalUrl(
            publisher, "https://www.prothomalo.com/bangladesh/story"))
        .thenReturn(true);
    AllowlistedPublisherIngestionService service =
        service(feedClient, mock(DirectArticleScraper.class));

    IngestionResult result = service.ingestPublisher(publisherConfig());

    assertThat(result).isEqualTo(new IngestionResult(2, 0, 2));
    verify(rawContentRepository, never()).save(any());
  }

  private AllowlistedPublisherIngestionService service(
      FeedClient feedClient, DirectArticleScraper articleScraper) {
    IngestionProperties properties = new IngestionProperties();
    properties.setPublishers(List.of(publisherConfig()));
    return new AllowlistedPublisherIngestionService(
        properties,
        feedClient,
        articleScraper,
        new CanonicalUrlNormalizer(),
        new ContentHashGenerator(),
        rawContentRepository,
        publisherResolver,
        CLOCK,
        metricsService);
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
}
