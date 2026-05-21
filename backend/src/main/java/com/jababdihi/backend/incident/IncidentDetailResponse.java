package com.jababdihi.backend.incident;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record IncidentDetailResponse(
    UUID id,
    String actorRole,
    String actorColor,
    String title,
    String summary,
    List<String> categories,
    LocalDate incidentDate,
    LocationResponse location,
    ConfidenceResponse confidence,
    List<SourceResponse> sources,
    String disclaimer) {}
