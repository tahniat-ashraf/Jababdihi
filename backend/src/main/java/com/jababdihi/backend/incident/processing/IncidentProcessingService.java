package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.IncidentStatus;
import com.jababdihi.backend.incident.Category;
import com.jababdihi.backend.incident.CategoryRepository;
import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.incident.IncidentRepository;
import com.jababdihi.backend.incident.IncidentTranslation;
import com.jababdihi.backend.ingestion.RawContent;
import com.jababdihi.backend.ingestion.RawContentRepository;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.location.LocationRepository;
import com.jababdihi.backend.source.IncidentSource;
import com.jababdihi.backend.source.IncidentSourceRepository;
import com.jababdihi.backend.source.SourceType;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("worker")
public class IncidentProcessingService {
  private static final String RAW_STATUS_PENDING = "PENDING";
  private static final String RAW_STATUS_PROCESSED = "PROCESSED";

  private final RawContentRepository rawContentRepository;
  private final IncidentRepository incidentRepository;
  private final CategoryRepository categoryRepository;
  private final LocationRepository locationRepository;
  private final IncidentSourceRepository incidentSourceRepository;
  private final IncidentExtractor incidentExtractor;
  private final DeduplicationService deduplicationService;
  private final ConfidenceScoringService confidenceScoringService;
  private final Clock clock;

  IncidentProcessingService(
      RawContentRepository rawContentRepository,
      IncidentRepository incidentRepository,
      CategoryRepository categoryRepository,
      LocationRepository locationRepository,
      IncidentSourceRepository incidentSourceRepository,
      IncidentExtractor incidentExtractor,
      DeduplicationService deduplicationService,
      ConfidenceScoringService confidenceScoringService,
      Clock clock) {
    this.rawContentRepository = rawContentRepository;
    this.incidentRepository = incidentRepository;
    this.categoryRepository = categoryRepository;
    this.locationRepository = locationRepository;
    this.incidentSourceRepository = incidentSourceRepository;
    this.incidentExtractor = incidentExtractor;
    this.deduplicationService = deduplicationService;
    this.confidenceScoringService = confidenceScoringService;
    this.clock = clock;
  }

  @Transactional
  public List<IncidentProcessingResult> processPendingRawContents() {
    return rawContentRepository
        .findTop100ByAiProcessingStatusOrderByFetchedAtAsc(RAW_STATUS_PENDING)
        .stream()
        .map(rawContent -> processRawContent(rawContent.getId()))
        .toList();
  }

  @Transactional
  public IncidentProcessingResult processRawContent(UUID rawContentId) {
    RawContent rawContent =
        rawContentRepository
            .findById(rawContentId)
            .orElseThrow(
                () -> new IllegalArgumentException("Raw content not found: " + rawContentId));

    Optional<IncidentSource> existingSource =
        incidentSourceRepository.findByCanonicalUrl(rawContent.getCanonicalUrl());
    if (existingSource.isPresent()) {
      markRawContentProcessed(rawContent, existingSource.get().getIncident(), null);
      return new IncidentProcessingResult(
          rawContent.getId(),
          existingSource.get().getIncident().getId(),
          existingSource.get().getIncident().getStatus(),
          DeduplicationAction.AUTO_MERGE);
    }

    IncidentCandidate candidate = incidentExtractor.extract(rawContent);
    applyExtractionMetadata(rawContent, candidate);
    DeduplicationDecision deduplicationDecision = deduplicationService.findBestMatch(candidate);
    Incident incident =
        deduplicationDecision.action() == DeduplicationAction.AUTO_MERGE
            ? deduplicationDecision.matchedIncident().orElseThrow()
            : createIncident(candidate);

    mergeCandidateMetadata(incident, candidate);
    attachSource(incident, rawContent);
    recomputeIncidentStatusAndConfidence(incident, candidate);
    incidentRepository.save(incident);
    markRawContentProcessed(rawContent, incident, candidate);

    return new IncidentProcessingResult(
        rawContent.getId(), incident.getId(), incident.getStatus(), deduplicationDecision.action());
  }

  private Incident createIncident(IncidentCandidate candidate) {
    Incident incident = new Incident();
    incident.setActorRole(candidate.actorRole());
    incident.setIncidentDate(candidate.incidentDate());
    incident.setExtractedLocationText(candidate.locationExtraction().extractedText());
    incident.setLocation(createLocation(candidate.locationExtraction()));
    incident.setStatus(IncidentStatus.AI_EXTRACTED);
    Instant now = Instant.now(clock);
    incident.setCreatedAt(now);
    incident.setUpdatedAt(now);
    incident.getCategories().add(category(candidate));
    incidentRepository.save(incident);
    addPlaceholderTranslations(incident, candidate);
    return incident;
  }

  private void mergeCandidateMetadata(Incident incident, IncidentCandidate candidate) {
    if (incident.getActorRole() == ActorRole.UNKNOWN
        && candidate.actorRole() != ActorRole.UNKNOWN) {
      incident.setActorRole(candidate.actorRole());
    }
    if (incident.getIncidentDate() == null) {
      incident.setIncidentDate(candidate.incidentDate());
    }
    if (incident.getLocation() == null) {
      incident.setLocation(createLocation(candidate.locationExtraction()));
      incident.setExtractedLocationText(candidate.locationExtraction().extractedText());
    }
    incident.getCategories().add(category(candidate));
    incident.setPoliticalAccountabilityLink(
        incident.isPoliticalAccountabilityLink() || candidate.politicalAccountabilityLink());
    if (incident.getExtractionConfidence() == null
        || candidate.extractionConfidence().compareTo(incident.getExtractionConfidence()) > 0) {
      incident.setExtractionConfidence(candidate.extractionConfidence());
    }
  }

  private Category category(IncidentCandidate candidate) {
    return categoryRepository
        .findById(candidate.categoryCode().name())
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "Configured category is missing from database: " + candidate.categoryCode()));
  }

  private Location createLocation(LocationExtraction locationExtraction) {
    if (locationExtraction.district().isEmpty()) {
      return null;
    }

    Location location = new Location();
    location.setCountry("Bangladesh");
    location.setDistrict(locationExtraction.district().get());
    return locationRepository.save(location);
  }

  private void addPlaceholderTranslations(Incident incident, IncidentCandidate candidate) {
    incident
        .getTranslations()
        .add(new IncidentTranslation(incident, "en", candidate.title(), candidate.summary()));
    incident
        .getTranslations()
        .add(new IncidentTranslation(incident, "bn", candidate.title(), candidate.summary()));
  }

  private void attachSource(Incident incident, RawContent rawContent) {
    IncidentSource source = new IncidentSource();
    source.setIncident(incident);
    source.setPublisher(rawContent.getPublisher());
    source.setSourceUrl(rawContent.getSourceUrl());
    source.setCanonicalUrl(rawContent.getCanonicalUrl());
    source.setSourceTitle(rawContent.getSourceTitle());
    source.setRelevantExcerpt(rawContent.getRelevantExcerpt());
    source.setPublishedAt(rawContent.getPublishedAt());
    source.setFetchedAt(rawContent.getFetchedAt());
    source.setSourceType(SourceType.valueOf(rawContent.getPublisher().getType().name()));
    source.setAiRelevanceScore(BigDecimal.valueOf(100));
    source.setCreatedAt(Instant.now(clock));
    incident.getSources().add(source);
  }

  private void recomputeIncidentStatusAndConfidence(
      Incident incident, IncidentCandidate candidate) {
    incident.setSourceCount(incident.getSources().size());
    incident.setIndependentPublisherCount(independentPublisherCount(incident));
    BigDecimal confidenceScore = confidenceScoringService.score(incident);
    incident.setConfidenceScore(confidenceScore);
    incident.setConfidenceLevel(confidenceScoringService.confidenceLevel(confidenceScore));
    incident.setUpdatedAt(Instant.now(clock));
    incident.setStatus(IncidentStatus.AUTO_PUBLISHED);
  }

  private int independentPublisherCount(Incident incident) {
    Set<String> publisherDomains =
        incident.getSources().stream()
            .map(source -> source.getPublisher().getDomain())
            .collect(Collectors.toSet());
    return publisherDomains.size();
  }

  private void applyExtractionMetadata(RawContent rawContent, IncidentCandidate candidate) {
    rawContent.setPoliticalAccountabilityLink(candidate.politicalAccountabilityLink());
    rawContent.setExtractedActorRole(candidate.actorRole().name());
    rawContent.setExtractedCategoryCode(candidate.categoryCode().name());
    rawContent.setExtractionConfidence(candidate.extractionConfidence());
  }

  private void markRawContentProcessed(
      RawContent rawContent, Incident incident, IncidentCandidate candidate) {
    if (candidate != null) {
      applyExtractionMetadata(rawContent, candidate);
    }
    rawContent.setProcessedIncidentId(incident.getId());
    rawContent.setProcessedAt(Instant.now(clock));
    rawContent.setAiProcessingStatus(RAW_STATUS_PROCESSED);
  }
}
