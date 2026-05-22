package com.jababdihi.backend.incident.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.incident.Category;
import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.incident.IncidentRepository;
import com.jababdihi.backend.incident.IncidentTranslation;
import com.jababdihi.backend.location.Location;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TitleDateCategoryLocationDeduplicationServiceTests {
  private final IncidentRepository incidentRepository = mock(IncidentRepository.class);
  private final IncidentProcessingProperties properties =
      RuleBasedIncidentExtractorTests.properties();
  private final TitleDateCategoryLocationDeduplicationService service =
      new TitleDateCategoryLocationDeduplicationService(incidentRepository, properties);

  @Test
  void findBestMatchAutoMergesWhenFallbackScoreExceedsThreshold() {
    Incident existingIncident = incident("Police official accused of extortion in Dhaka");
    when(incidentRepository.findTop100ByIncidentDateOrderByCreatedAtDesc(LocalDate.of(2026, 5, 22)))
        .thenReturn(List.of(existingIncident));

    DeduplicationDecision decision =
        service.findBestMatch(
            new IncidentCandidate(
                "Police official accused of extortion in Dhaka",
                "summary",
                ActorRole.GOVERNMENT,
                CategoryCode.EXTORTION,
                true,
                LocalDate.of(2026, 5, 22),
                new LocationExtraction(Optional.of("Dhaka"), "Dhaka"),
                true,
                BigDecimal.valueOf(0.90)));

    assertThat(decision.action()).isEqualTo(DeduplicationAction.AUTO_MERGE);
    assertThat(decision.score()).isGreaterThanOrEqualTo(0.86);
  }

  @Test
  void findBestMatchCreatesNewIncidentBelowManualReviewThreshold() {
    Incident existingIncident = incident("Unrelated corruption report");
    when(incidentRepository.findTop100ByIncidentDateOrderByCreatedAtDesc(LocalDate.of(2026, 5, 22)))
        .thenReturn(List.of(existingIncident));

    DeduplicationDecision decision =
        service.findBestMatch(
            new IncidentCandidate(
                "Police official accused of extortion in Dhaka",
                "summary",
                ActorRole.GOVERNMENT,
                CategoryCode.EXTORTION,
                true,
                LocalDate.of(2026, 5, 22),
                new LocationExtraction(Optional.of("Dhaka"), "Dhaka"),
                true,
                BigDecimal.valueOf(0.90)));

    assertThat(decision.action()).isEqualTo(DeduplicationAction.NEW_INCIDENT);
    assertThat(decision.score()).isLessThan(0.72);
  }

  private Incident incident(String title) {
    Incident incident = new Incident();
    incident.setIncidentDate(LocalDate.of(2026, 5, 22));
    Location location = new Location();
    location.setDistrict("Dhaka");
    incident.setLocation(location);
    Category category = new Category();
    category.setCode("EXTORTION");
    incident.getCategories().add(category);
    incident.getTranslations().add(new IncidentTranslation(incident, "en", title, "summary"));
    return incident;
  }
}
