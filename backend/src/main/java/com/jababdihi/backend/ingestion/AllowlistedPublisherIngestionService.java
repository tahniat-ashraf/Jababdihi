package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.observability.OperationalMetricsService;
import com.jababdihi.backend.source.Publisher;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("worker")
public class AllowlistedPublisherIngestionService {
  private static final String PARSING_STATUS_PARSED = "PARSED";
  private static final String AI_PROCESSING_STATUS_PENDING = "PENDING";

  private final IngestionProperties ingestionProperties;
  private final FeedClient feedClient;
  private final DirectArticleScraper articleScraper;
  private final CanonicalUrlNormalizer canonicalUrlNormalizer;
  private final ContentHashGenerator contentHashGenerator;
  private final RawContentRepository rawContentRepository;
  private final PublisherResolver publisherResolver;
  private final Clock clock;
  private final OperationalMetricsService metricsService;

  AllowlistedPublisherIngestionService(
      IngestionProperties ingestionProperties,
      FeedClient feedClient,
      DirectArticleScraper articleScraper,
      CanonicalUrlNormalizer canonicalUrlNormalizer,
      ContentHashGenerator contentHashGenerator,
      RawContentRepository rawContentRepository,
      PublisherResolver publisherResolver,
      Clock clock,
      OperationalMetricsService metricsService) {
    this.ingestionProperties = ingestionProperties;
    this.feedClient = feedClient;
    this.articleScraper = articleScraper;
    this.canonicalUrlNormalizer = canonicalUrlNormalizer;
    this.contentHashGenerator = contentHashGenerator;
    this.rawContentRepository = rawContentRepository;
    this.publisherResolver = publisherResolver;
    this.clock = clock;
    this.metricsService = metricsService;
  }

  @Transactional
  public IngestionResult ingestAllAllowlistedPublishers() {
    int fetchedItems = 0;
    int storedItems = 0;
    int skippedItems = 0;

    for (IngestionProperties.PublisherFeedProperties publisher :
        ingestionProperties.getPublishers()) {
      IngestionResult result = ingestPublisher(publisher);
      fetchedItems += result.fetchedItems();
      storedItems += result.storedItems();
      skippedItems += result.skippedItems();
    }

    return new IngestionResult(fetchedItems, storedItems, skippedItems);
  }

  @Transactional
  public IngestionResult ingestPublisher(
      IngestionProperties.PublisherFeedProperties publisherConfig) {
    Publisher publisher = publisherResolver.resolve(publisherConfig);
    int fetchedItems = 0;
    int storedItems = 0;
    int skippedItems = 0;

    for (FeedItem item : fetchFeedItems(publisherConfig)) {
      fetchedItems++;
      metricsService.recordCrawlerFetch(publisherConfig.getName(), "fetched");
      Optional<RawContent> rawContent = createRawContent(publisher, publisherConfig, item);
      if (rawContent.isEmpty()) {
        skippedItems++;
        metricsService.recordCrawlerFetch(publisherConfig.getName(), "skipped");
        continue;
      }

      rawContentRepository.save(rawContent.get());
      storedItems++;
      metricsService.recordCrawlerFetch(publisherConfig.getName(), "stored");
    }

    return new IngestionResult(fetchedItems, storedItems, skippedItems);
  }

  @Transactional
  public CurrentPublisherIngestionResult ingestCurrentPublisher(
      Publisher publisher,
      IngestionProperties.PublisherFeedProperties publisherConfig,
      Instant cursorPublishedAt,
      int itemLimit,
      Instant deadline) {
    int fetchedItems = 0;
    int attemptedItems = 0;
    int storedItems = 0;
    int skippedItems = 0;
    Optional<Instant> latestCursor = Optional.of(cursorPublishedAt);
    List<UUID> rawContentIds = new ArrayList<>();

    for (FeedItem item : currentItems(publisherConfig, cursorPublishedAt)) {
      if ((itemLimit >= 0 && attemptedItems >= itemLimit)
          || !Instant.now(clock).isBefore(deadline)) {
        break;
      }
      fetchedItems++;

      ArticleContent content = resolveArticleContentForCurrent(publisherConfig, item);
      if (content.publishedAt().isEmpty()
          || !content.publishedAt().get().isAfter(cursorPublishedAt)) {
        skippedItems++;
        metricsService.recordCrawlerFetch(publisherConfig.getName(), "skipped");
        continue;
      }

      attemptedItems++;
      latestCursor = max(latestCursor, content.publishedAt().get());
      metricsService.recordCrawlerFetch(publisherConfig.getName(), "fetched");
      Optional<RawContent> rawContent = createRawContent(publisher, publisherConfig, item, content);
      if (rawContent.isEmpty()) {
        skippedItems++;
        metricsService.recordCrawlerFetch(publisherConfig.getName(), "skipped");
        continue;
      }

      RawContent saved = rawContentRepository.save(rawContent.get());
      rawContentIds.add(saved.getId());
      storedItems++;
      metricsService.recordCrawlerFetch(publisherConfig.getName(), "stored");
    }

    return new CurrentPublisherIngestionResult(
        fetchedItems, attemptedItems, storedItems, skippedItems, latestCursor, rawContentIds);
  }

  private List<FeedItem> fetchFeedItems(IngestionProperties.PublisherFeedProperties publisher) {
    try {
      return feedClient.fetch(publisher);
    } catch (RuntimeException ex) {
      metricsService.recordCrawlerFetch(publisher.getName(), "failed");
      throw ex;
    }
  }

  private Optional<RawContent> createRawContent(
      Publisher publisher,
      IngestionProperties.PublisherFeedProperties publisherConfig,
      FeedItem item) {
    Optional<String> canonicalUrl =
        canonicalUrlNormalizer.normalize(item.sourceUrl(), publisherConfig.getDomain());
    if (canonicalUrl.isEmpty()
        || rawContentRepository.existsByPublisherAndCanonicalUrl(publisher, canonicalUrl.get())) {
      return Optional.empty();
    }

    ArticleContent content = resolveArticleContent(canonicalUrl.get(), publisherConfig, item);
    return createRawContent(publisher, publisherConfig, item, content);
  }

  private Optional<RawContent> createRawContent(
      Publisher publisher,
      IngestionProperties.PublisherFeedProperties publisherConfig,
      FeedItem item,
      ArticleContent content) {
    Optional<String> canonicalUrl =
        canonicalUrlNormalizer.normalize(item.sourceUrl(), publisherConfig.getDomain());
    if (canonicalUrl.isEmpty()
        || rawContentRepository.existsByPublisherAndCanonicalUrl(publisher, canonicalUrl.get())) {
      return Optional.empty();
    }

    if (content.extractedText().isBlank()) {
      return Optional.empty();
    }

    String contentHash = contentHashGenerator.hash(content.title(), content.extractedText());
    if (rawContentRepository.existsByContentHash(contentHash)) {
      return Optional.empty();
    }

    RawContent rawContent = new RawContent();
    rawContent.setPublisher(publisher);
    rawContent.setSourceUrl(item.sourceUrl());
    rawContent.setCanonicalUrl(canonicalUrl.get());
    rawContent.setSourceTitle(content.title());
    rawContent.setPublishedAt(content.publishedAt().orElse(null));
    rawContent.setExtractedText(content.extractedText());
    rawContent.setRelevantExcerpt("");
    rawContent.setContentHash(contentHash);
    rawContent.setLanguageCode(publisherConfig.getLanguageCode());
    rawContent.setFetchedAt(Instant.now(clock));
    rawContent.setParsingStatus(PARSING_STATUS_PARSED);
    rawContent.setAiProcessingStatus(AI_PROCESSING_STATUS_PENDING);
    return Optional.of(rawContent);
  }

  private List<FeedItem> currentItems(
      IngestionProperties.PublisherFeedProperties publisherConfig, Instant cursorPublishedAt) {
    return fetchFeedItems(publisherConfig).stream()
        .filter(
            item ->
                item.publishedAt().isEmpty() || item.publishedAt().get().isAfter(cursorPublishedAt))
        .sorted(Comparator.comparing(item -> item.publishedAt().orElse(Instant.MAX)))
        .toList();
  }

  private ArticleContent resolveArticleContentForCurrent(
      IngestionProperties.PublisherFeedProperties publisherConfig, FeedItem item) {
    Optional<String> canonicalUrl =
        canonicalUrlNormalizer.normalize(item.sourceUrl(), publisherConfig.getDomain());
    if (canonicalUrl.isEmpty()) {
      return new ArticleContent(item.title(), item.publishedAt(), "");
    }
    return resolveArticleContent(canonicalUrl.get(), publisherConfig, item);
  }

  private ArticleContent resolveArticleContent(
      String canonicalUrl,
      IngestionProperties.PublisherFeedProperties publisherConfig,
      FeedItem item) {
    if (!item.extractedText().isBlank()) {
      return new ArticleContent(item.title(), item.publishedAt(), item.extractedText());
    }

    Optional<ScrapedArticle> scrapedArticle = articleScraper.scrape(canonicalUrl, publisherConfig);
    if (scrapedArticle.isEmpty()) {
      return new ArticleContent(item.title(), item.publishedAt(), "");
    }

    ScrapedArticle article = scrapedArticle.get();
    String title = article.title().isBlank() ? item.title() : article.title();
    Optional<Instant> publishedAt =
        article.publishedAt().isPresent() ? article.publishedAt() : item.publishedAt();
    return new ArticleContent(title, publishedAt, article.extractedText());
  }

  private record ArticleContent(
      String title, Optional<Instant> publishedAt, String extractedText) {}

  private Optional<Instant> max(Optional<Instant> current, Instant candidate) {
    if (current.isEmpty() || candidate.isAfter(current.get())) {
      return Optional.of(candidate);
    }
    return current;
  }
}
