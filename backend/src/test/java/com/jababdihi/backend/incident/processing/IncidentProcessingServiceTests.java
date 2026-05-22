package com.jababdihi.backend.incident.processing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.common.IncidentStatus;
import com.jababdihi.backend.incident.Category;
import com.jababdihi.backend.incident.CategoryRepository;
import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.incident.IncidentRepository;
import com.jababdihi.backend.ingestion.RawContent;
import com.jababdihi.backend.ingestion.RawContentRepository;
import com.jababdihi.backend.location.LocationRepository;
import com.jababdihi.backend.source.IncidentSource;
import com.jababdihi.backend.source.IncidentSourceRepository;
import com.jababdihi.backend.source.Publisher;
import com.jababdihi.backend.source.PublisherType;
import com.jababdihi.backend.source.SourceType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class IncidentProcessingServiceTests {
  private static final Instant NOW = Instant.parse("2026-05-22T12:00:00Z");
  private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

  private final RawContentRepository rawContentRepository = mock(RawContentRepository.class);
  private final IncidentRepository incidentRepository = mock(IncidentRepository.class);
  private final CategoryRepository categoryRepository = mock(CategoryRepository.class);
  private final LocationRepository locationRepository = mock(LocationRepository.class);
  private final IncidentSourceRepository incidentSourceRepository =
      mock(IncidentSourceRepository.class);
  private final IncidentExtractor incidentExtractor = mock(IncidentExtractor.class);
  private final DeduplicationService deduplicationService = mock(DeduplicationService.class);
  private final IncidentProcessingService service =
      new IncidentProcessingService(
          rawContentRepository,
          incidentRepository,
          categoryRepository,
          locationRepository,
          incidentSourceRepository,
          incidentExtractor,
          deduplicationService,
          new ConfidenceScoringService(),
          CLOCK);

  @Test
  void processRawContentAutoPublishesMergedIncidentWhenCriteriaPass() {
    UUID rawContentId = UUID.randomUUID();
    RawContent rawContent = rawContent(rawContentId);
    Incident existingIncident = existingIncident();
    IncidentCandidate candidate =
        new IncidentCandidate(
            "Police official accused of extortion in Dhaka",
            "summary",
            ActorRole.GOVERNMENT,
            CategoryCode.EXTORTION,
            true,
            LocalDate.of(2026, 5, 22),
            new LocationExtraction(Optional.empty(), ""),
            true,
            BigDecimal.valueOf(0.90));
    Category category = new Category();
    category.setCode("EXTORTION");

    when(rawContentRepository.findById(rawContentId)).thenReturn(Optional.of(rawContent));
    when(incidentSourceRepository.findByCanonicalUrl(rawContent.getCanonicalUrl()))
        .thenReturn(Optional.empty());
    when(incidentExtractor.extract(rawContent)).thenReturn(candidate);
    when(deduplicationService.findBestMatch(candidate))
        .thenReturn(
            new DeduplicationDecision(
                DeduplicationAction.AUTO_MERGE, Optional.of(existingIncident), 1.0));
    when(categoryRepository.findById("EXTORTION")).thenReturn(Optional.of(category));
    when(incidentRepository.save(any(Incident.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    IncidentProcessingResult result = service.processRawContent(rawContentId);

    assertThat(result.status()).isEqualTo(IncidentStatus.AUTO_PUBLISHED);
    assertThat(existingIncident.getStatus()).isEqualTo(IncidentStatus.AUTO_PUBLISHED);
    assertThat(existingIncident.getSourceCount()).isEqualTo(2);
    assertThat(existingIncident.getIndependentPublisherCount()).isEqualTo(2);
    assertThat(rawContent.getAiProcessingStatus()).isEqualTo("PROCESSED");
    assertThat(rawContent.isPoliticalAccountabilityLink()).isTrue();
    assertThat(rawContent.getExtractedActorRole()).isEqualTo("GOVERNMENT");
    assertThat(rawContent.getExtractedCategoryCode()).isEqualTo("EXTORTION");
    verify(incidentRepository).save(existingIncident);
  }

  private RawContent rawContent(UUID id) {
    RawContent rawContent = new RawContent();
    org.springframework.test.util.ReflectionTestUtils.setField(rawContent, "id", id);
    rawContent.setPublisher(publisher("thedailystar.net"));
    rawContent.setSourceUrl("https://www.thedailystar.net/news/story");
    rawContent.setCanonicalUrl("https://www.thedailystar.net/news/story");
    rawContent.setSourceTitle("Police official accused of extortion in Dhaka");
    rawContent.setRelevantExcerpt("");
    rawContent.setPublishedAt(Instant.parse("2026-05-22T08:00:00Z"));
    rawContent.setFetchedAt(NOW);
    return rawContent;
  }

  private Incident existingIncident() {
    Incident incident = new Incident();
    incident.setActorRole(ActorRole.GOVERNMENT);
    incident.setIncidentDate(LocalDate.of(2026, 5, 22));
    incident.setStatus(IncidentStatus.PENDING_REVIEW);
    incident.setPoliticalAccountabilityLink(true);
    incident.setExtractionConfidence(BigDecimal.valueOf(0.90));
    IncidentSource source = new IncidentSource();
    source.setIncident(incident);
    source.setPublisher(publisher("prothomalo.com"));
    source.setSourceType(SourceType.NEWSPAPER);
    source.setPublishedAt(Instant.parse("2026-05-22T07:00:00Z"));
    incident.getSources().add(source);
    incident.setSourceCount(1);
    incident.setIndependentPublisherCount(1);
    return incident;
  }

  private Publisher publisher(String domain) {
    Publisher publisher = new Publisher();
    publisher.setName(domain);
    publisher.setDomain(domain);
    publisher.setType(PublisherType.NEWSPAPER);
    return publisher;
  }
}
