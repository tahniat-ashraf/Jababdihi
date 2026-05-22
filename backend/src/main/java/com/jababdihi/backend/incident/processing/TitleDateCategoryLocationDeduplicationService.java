package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.incident.IncidentRepository;
import com.jababdihi.backend.incident.IncidentTranslation;
import com.jababdihi.backend.location.Location;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class TitleDateCategoryLocationDeduplicationService implements DeduplicationService {
  private final IncidentRepository incidentRepository;
  private final IncidentProcessingProperties properties;

  TitleDateCategoryLocationDeduplicationService(
      IncidentRepository incidentRepository, IncidentProcessingProperties properties) {
    this.incidentRepository = incidentRepository;
    this.properties = properties;
  }

  @Override
  public DeduplicationDecision findBestMatch(IncidentCandidate candidate) {
    List<Incident> incidents =
        candidate.incidentDate() == null
            ? incidentRepository.findTop100ByOrderByCreatedAtDesc()
            : incidentRepository.findTop100ByIncidentDateOrderByCreatedAtDesc(
                candidate.incidentDate());
    Optional<ScoredIncident> bestMatch =
        incidents.stream()
            .map(incident -> new ScoredIncident(incident, score(candidate, incident)))
            .max(ScoredIncident::compareTo);

    if (bestMatch.isEmpty()) {
      return DeduplicationDecision.newIncident(0);
    }

    ScoredIncident scoredIncident = bestMatch.get();
    double autoMergeThreshold = properties.getDeduplication().getAutoMergeThreshold();
    double manualReviewThreshold = properties.getDeduplication().getManualReviewThreshold();
    if (scoredIncident.score() >= autoMergeThreshold) {
      return new DeduplicationDecision(
          DeduplicationAction.AUTO_MERGE,
          Optional.of(scoredIncident.incident()),
          scoredIncident.score());
    }
    if (scoredIncident.score() >= manualReviewThreshold) {
      return new DeduplicationDecision(
          DeduplicationAction.MANUAL_REVIEW,
          Optional.of(scoredIncident.incident()),
          scoredIncident.score());
    }
    return DeduplicationDecision.newIncident(scoredIncident.score());
  }

  private double score(IncidentCandidate candidate, Incident incident) {
    double titleScore = titleSimilarity(candidate.title(), titleFromIncident(incident)) * 0.45;
    double dateScore = dateMatches(candidate, incident) ? 0.20 : 0;
    double categoryScore = categoryMatches(candidate, incident) ? 0.20 : 0;
    double locationScore = locationMatches(candidate, incident) ? 0.15 : 0;
    return titleScore + dateScore + categoryScore + locationScore;
  }

  double titleSimilarity(String leftTitle, String rightTitle) {
    Set<String> leftTokens = tokenize(leftTitle);
    Set<String> rightTokens = tokenize(rightTitle);
    if (leftTokens.isEmpty() || rightTokens.isEmpty()) {
      return 0;
    }

    long intersection = leftTokens.stream().filter(rightTokens::contains).count();
    long union = leftTokens.size() + rightTokens.size() - intersection;
    return union == 0 ? 0 : (double) intersection / union;
  }

  private String titleFromIncident(Incident incident) {
    return incident.getTranslations().stream()
        .map(IncidentTranslation::getTitle)
        .filter(title -> title != null && !title.isBlank())
        .findFirst()
        .orElse("");
  }

  private Set<String> tokenize(String title) {
    if (title == null || title.isBlank()) {
      return Set.of();
    }
    return Arrays.stream(title.toLowerCase(Locale.ROOT).split("[^\\p{L}\\p{N}]+"))
        .filter(token -> token.length() > 2)
        .collect(Collectors.toSet());
  }

  private boolean dateMatches(IncidentCandidate candidate, Incident incident) {
    return candidate.incidentDate() != null
        && candidate.incidentDate().equals(incident.getIncidentDate());
  }

  private boolean categoryMatches(IncidentCandidate candidate, Incident incident) {
    return incident.getCategories().stream()
        .anyMatch(category -> category.getCode().equals(candidate.categoryCode().name()));
  }

  private boolean locationMatches(IncidentCandidate candidate, Incident incident) {
    Optional<String> candidateDistrict = candidate.locationExtraction().district();
    Location incidentLocation = incident.getLocation();
    if (candidateDistrict.isEmpty() && incidentLocation == null) {
      return true;
    }
    return candidateDistrict.isPresent()
        && incidentLocation != null
        && candidateDistrict.get().equalsIgnoreCase(incidentLocation.getDistrict());
  }

  private record ScoredIncident(Incident incident, double score)
      implements Comparable<ScoredIncident> {
    @Override
    public int compareTo(ScoredIncident other) {
      return Double.compare(score, other.score);
    }
  }
}
