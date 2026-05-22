package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.source.IncidentSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class ConfidenceScoringService {
  BigDecimal score(Incident incident) {
    double sourceCountScore = sourceCountScore(incident.getSourceCount());
    double independentPublisherScore =
        independentPublisherScore(incident.getIndependentPublisherCount());
    double metadataConsistencyScore = metadataConsistencyScore(incident);
    double score =
        sourceCountScore * 0.45
            + independentPublisherScore * 0.35
            + metadataConsistencyScore * 0.20;
    return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
  }

  String confidenceLevel(BigDecimal score) {
    if (score.compareTo(BigDecimal.valueOf(75)) >= 0) {
      return "HIGH";
    }
    if (score.compareTo(BigDecimal.valueOf(50)) >= 0) {
      return "MODERATE";
    }
    return "LOW";
  }

  private double sourceCountScore(int sourceCount) {
    if (sourceCount >= 8) {
      return 100;
    }
    if (sourceCount >= 5) {
      return 80;
    }
    if (sourceCount >= 3) {
      return 60;
    }
    if (sourceCount >= 2) {
      return 45;
    }
    return 20;
  }

  private double independentPublisherScore(int publisherCount) {
    if (publisherCount >= 5) {
      return 100;
    }
    if (publisherCount >= 3) {
      return 75;
    }
    if (publisherCount >= 2) {
      return 55;
    }
    return 20;
  }

  private double metadataConsistencyScore(Incident incident) {
    double score = 60;
    if (sourceDatesMatchIncidentDate(incident)) {
      score += 20;
    }
    if (!incident.getCategories().isEmpty()) {
      score += 10;
    }
    if (incident.getLocation() != null || noSourceHasPublicationDate(incident)) {
      score += 10;
    }
    return Math.min(score, 100);
  }

  private boolean sourceDatesMatchIncidentDate(Incident incident) {
    if (incident.getIncidentDate() == null) {
      return false;
    }

    Set<LocalDate> sourceDates =
        incident.getSources().stream()
            .map(IncidentSource::getPublishedAt)
            .filter(Objects::nonNull)
            .map(publishedAt -> LocalDate.ofInstant(publishedAt, ZoneOffset.UTC))
            .collect(Collectors.toSet());
    return sourceDates.isEmpty() || sourceDates.contains(incident.getIncidentDate());
  }

  private boolean noSourceHasPublicationDate(Incident incident) {
    return incident.getSources().stream()
        .map(IncidentSource::getPublishedAt)
        .allMatch(Objects::isNull);
  }
}
