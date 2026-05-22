package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
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

  AllowlistedPublisherIngestionService(
      IngestionProperties ingestionProperties,
      FeedClient feedClient,
      DirectArticleScraper articleScraper,
      CanonicalUrlNormalizer canonicalUrlNormalizer,
      ContentHashGenerator contentHashGenerator,
      RawContentRepository rawContentRepository,
      PublisherResolver publisherResolver,
      Clock clock) {
    this.ingestionProperties = ingestionProperties;
    this.feedClient = feedClient;
    this.articleScraper = articleScraper;
    this.canonicalUrlNormalizer = canonicalUrlNormalizer;
    this.contentHashGenerator = contentHashGenerator;
    this.rawContentRepository = rawContentRepository;
    this.publisherResolver = publisherResolver;
    this.clock = clock;
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

    for (FeedItem item : feedClient.fetch(publisherConfig)) {
      fetchedItems++;
      Optional<RawContent> rawContent = createRawContent(publisher, publisherConfig, item);
      if (rawContent.isEmpty()) {
        skippedItems++;
        continue;
      }

      rawContentRepository.save(rawContent.get());
      storedItems++;
    }

    return new IngestionResult(fetchedItems, storedItems, skippedItems);
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
}
