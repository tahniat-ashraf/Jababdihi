package com.jababdihi.backend.admin;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record AdminIncidentDetail(
    UUID id,
    String status,
    String actorRole,
    String title,
    String summary,
    List<String> categories,
    String locationDistrict,
    String locationDivision,
    int sourceCount,
    int independentPublisherCount,
    Integer confidenceScore,
    String confidenceLevel,
    LocalDate incidentDate,
    Instant createdAt,
    List<AdminSourceDetail> sources) {}
