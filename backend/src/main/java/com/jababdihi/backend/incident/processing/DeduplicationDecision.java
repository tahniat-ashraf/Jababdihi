package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.incident.Incident;
import java.util.Optional;

public record DeduplicationDecision(
    DeduplicationAction action, Optional<Incident> matchedIncident, double score) {
  static DeduplicationDecision newIncident(double score) {
    return new DeduplicationDecision(DeduplicationAction.NEW_INCIDENT, Optional.empty(), score);
  }
}
