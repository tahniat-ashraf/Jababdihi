package com.jababdihi.backend.incident.processing;

import static org.assertj.core.api.Assertions.assertThat;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.ingestion.RawContent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RuleBasedIncidentExtractorTests {

  @Test
  void extractAssignsPoliticalLinkActorCategoryLocationAndConfidence() {
    RuleBasedIncidentExtractor extractor = extractor(properties());
    RawContent rawContent = new RawContent();
    rawContent.setSourceTitle("Police official accused of extortion in Dhaka");
    rawContent.setExtractedText("Sources reported alleged extortion by a government officer.");
    rawContent.setPublishedAt(Instant.parse("2026-05-22T08:00:00Z"));

    IncidentCandidate candidate = extractor.extract(rawContent);

    assertThat(candidate.politicalAccountabilityLink()).isTrue();
    assertThat(candidate.actorRole()).isEqualTo(ActorRole.GOVERNMENT);
    assertThat(candidate.categoryCode()).isEqualTo(CategoryCode.EXTORTION);
    assertThat(candidate.categoryMatchedKeywordRule()).isTrue();
    assertThat(candidate.locationExtraction().district()).contains("Dhaka");
    assertThat(candidate.extractionConfidence()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
  }

  @Test
  void extractKeepsUnknownActorPendingQualityWhenRulesAreWeak() {
    RuleBasedIncidentExtractor extractor = extractor(properties());
    RawContent rawContent = new RawContent();
    rawContent.setSourceTitle("Local dispute reported");
    rawContent.setExtractedText(
        "A local dispute happened without clear public accountability context.");

    IncidentCandidate candidate = extractor.extract(rawContent);

    assertThat(candidate.politicalAccountabilityLink()).isFalse();
    assertThat(candidate.actorRole()).isEqualTo(ActorRole.UNKNOWN);
    assertThat(candidate.categoryCode()).isEqualTo(CategoryCode.ABUSE_OF_POWER);
    assertThat(candidate.categoryMatchedKeywordRule()).isFalse();
    assertThat(candidate.extractionConfidence()).isLessThan(BigDecimal.valueOf(0.75));
  }

  static RuleBasedIncidentExtractor extractor(IncidentProcessingProperties properties) {
    return new RuleBasedIncidentExtractor(
        properties,
        new KeywordCategoryAssignmentService(properties),
        new ActorRoleExtractor(properties),
        new PlaceholderLocationExtractionService(properties));
  }

  static IncidentProcessingProperties properties() {
    IncidentProcessingProperties properties = new IncidentProcessingProperties();
    properties.setPoliticalKeywords(List.of("government", "political", "party", "police"));
    properties.setAccountabilityKeywords(List.of("alleged", "accused", "reported", "extortion"));
    properties.setGovernmentActorKeywords(List.of("government", "police", "officer"));
    properties.setOppositionActorKeywords(List.of("opposition", "bnp"));
    properties.setDistrictKeywords(List.of("Dhaka"));
    IncidentProcessingProperties.CategoryRule extortion =
        new IncidentProcessingProperties.CategoryRule();
    extortion.setCode("EXTORTION");
    extortion.setKeywords(List.of("extortion"));
    properties.setCategoryRules(List.of(extortion));
    return properties;
  }
}
