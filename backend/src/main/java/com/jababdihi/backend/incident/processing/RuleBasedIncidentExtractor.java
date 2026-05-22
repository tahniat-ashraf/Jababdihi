package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.ingestion.RawContent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class RuleBasedIncidentExtractor implements IncidentExtractor {
  private static final int SUMMARY_MAX_LENGTH = 320;

  private final IncidentProcessingProperties properties;
  private final KeywordCategoryAssignmentService categoryAssignmentService;
  private final ActorRoleExtractor actorRoleExtractor;
  private final PlaceholderLocationExtractionService locationExtractionService;
  private final KeywordMatcher keywordMatcher = new KeywordMatcher();

  RuleBasedIncidentExtractor(
      IncidentProcessingProperties properties,
      KeywordCategoryAssignmentService categoryAssignmentService,
      ActorRoleExtractor actorRoleExtractor,
      PlaceholderLocationExtractionService locationExtractionService) {
    this.properties = properties;
    this.categoryAssignmentService = categoryAssignmentService;
    this.actorRoleExtractor = actorRoleExtractor;
    this.locationExtractionService = locationExtractionService;
  }

  @Override
  public IncidentCandidate extract(RawContent rawContent) {
    String title = safe(rawContent.getSourceTitle());
    String text = safe(rawContent.getExtractedText());
    CategoryAssignment category = categoryAssignmentService.assign(title, text);
    ActorRole actorRole = actorRoleExtractor.extract(title, text);
    LocationExtraction location = locationExtractionService.extract(title, text);
    boolean politicalAccountabilityLink =
        hasPoliticalAccountabilityLink(title, text, actorRole, category);
    BigDecimal extractionConfidence =
        extractionConfidence(rawContent, politicalAccountabilityLink, actorRole, category);

    return new IncidentCandidate(
        title.isBlank() ? "Untitled reported incident" : title,
        summary(text),
        actorRole,
        category.categoryCode(),
        category.matchedKeywordRule(),
        rawContent.getPublishedAt() == null
            ? null
            : LocalDate.ofInstant(rawContent.getPublishedAt(), ZoneOffset.UTC),
        location,
        politicalAccountabilityLink,
        extractionConfidence);
  }

  private boolean hasPoliticalAccountabilityLink(
      String title, String text, ActorRole actorRole, CategoryAssignment category) {
    String searchableText = title + "\n" + text;
    boolean politicalContext =
        actorRole != ActorRole.UNKNOWN
            || keywordMatcher.containsAny(searchableText, properties.getPoliticalKeywords());
    boolean accountabilityContext =
        category.matchedKeywordRule()
            || keywordMatcher.containsAny(searchableText, properties.getAccountabilityKeywords());
    return politicalContext && accountabilityContext;
  }

  private BigDecimal extractionConfidence(
      RawContent rawContent,
      boolean politicalAccountabilityLink,
      ActorRole actorRole,
      CategoryAssignment category) {
    double score = 0;
    if (politicalAccountabilityLink) {
      score += 0.35;
    }
    if (actorRole != ActorRole.UNKNOWN) {
      score += 0.25;
    }
    if (category.matchedKeywordRule()) {
      score += 0.20;
    }
    if (rawContent.getSourceTitle() != null && !rawContent.getSourceTitle().isBlank()) {
      score += 0.10;
    }
    if (rawContent.getPublishedAt() != null) {
      score += 0.10;
    }
    return BigDecimal.valueOf(Math.min(score, 1.0)).setScale(2, RoundingMode.HALF_UP);
  }

  private String summary(String text) {
    String normalizedText = safe(text).replaceAll("\\s+", " ").trim();
    if (normalizedText.length() <= SUMMARY_MAX_LENGTH) {
      return normalizedText;
    }
    return normalizedText.substring(0, SUMMARY_MAX_LENGTH).trim();
  }

  private String safe(String value) {
    return value == null ? "" : value;
  }
}
