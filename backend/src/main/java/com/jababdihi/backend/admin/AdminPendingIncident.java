package com.jababdihi.backend.admin;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record AdminPendingIncident(
    UUID id,
    String status,
    String actorRole,
    String title,
    String summary,
    int sourceCount,
    int independentPublisherCount,
    Integer confidenceScore,
    String confidenceLevel,
    LocalDate incidentDate,
    Instant createdAt) {}
