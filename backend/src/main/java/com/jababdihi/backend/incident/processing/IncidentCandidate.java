package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import java.math.BigDecimal;
import java.time.LocalDate;

public record IncidentCandidate(
    String title,
    String summary,
    ActorRole actorRole,
    CategoryCode categoryCode,
    boolean categoryMatchedKeywordRule,
    LocalDate incidentDate,
    LocationExtraction locationExtraction,
    boolean politicalAccountabilityLink,
    BigDecimal extractionConfidence) {}
