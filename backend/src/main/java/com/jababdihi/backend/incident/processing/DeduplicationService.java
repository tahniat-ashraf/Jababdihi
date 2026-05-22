package com.jababdihi.backend.incident.processing;

public interface DeduplicationService {
  DeduplicationDecision findBestMatch(IncidentCandidate candidate);
}
