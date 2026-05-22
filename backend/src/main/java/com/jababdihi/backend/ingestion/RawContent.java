package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raw_contents")
public class RawContent {
  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "publisher_id")
  private Publisher publisher;

  private String sourceUrl;
  private String canonicalUrl;
  private String sourceTitle;
  private String extractedText;
  private String relevantExcerpt;
  private String contentHash;
  private String languageCode;
  private Instant publishedAt;
  private Instant fetchedAt;
  private String parsingStatus;
  private String aiProcessingStatus;

  protected RawContent() {}

  public UUID getId() {
    return id;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getCanonicalUrl() {
    return canonicalUrl;
  }

  public void setCanonicalUrl(String canonicalUrl) {
    this.canonicalUrl = canonicalUrl;
  }

  public String getSourceTitle() {
    return sourceTitle;
  }

  public void setSourceTitle(String sourceTitle) {
    this.sourceTitle = sourceTitle;
  }

  public String getExtractedText() {
    return extractedText;
  }

  public void setExtractedText(String extractedText) {
    this.extractedText = extractedText;
  }

  public String getRelevantExcerpt() {
    return relevantExcerpt;
  }

  public void setRelevantExcerpt(String relevantExcerpt) {
    this.relevantExcerpt = relevantExcerpt;
  }

  public String getContentHash() {
    return contentHash;
  }

  public void setContentHash(String contentHash) {
    this.contentHash = contentHash;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }

  public Instant getPublishedAt() {
    return publishedAt;
  }

  public void setPublishedAt(Instant publishedAt) {
    this.publishedAt = publishedAt;
  }

  public Instant getFetchedAt() {
    return fetchedAt;
  }

  public void setFetchedAt(Instant fetchedAt) {
    this.fetchedAt = fetchedAt;
  }

  public String getParsingStatus() {
    return parsingStatus;
  }

  public void setParsingStatus(String parsingStatus) {
    this.parsingStatus = parsingStatus;
  }

  public String getAiProcessingStatus() {
    return aiProcessingStatus;
  }

  public void setAiProcessingStatus(String aiProcessingStatus) {
    this.aiProcessingStatus = aiProcessingStatus;
  }
}
