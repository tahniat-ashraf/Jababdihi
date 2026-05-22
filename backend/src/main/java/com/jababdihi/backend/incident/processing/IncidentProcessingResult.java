package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.IncidentStatus;
import java.util.UUID;

public record IncidentProcessingResult(
    UUID rawContentId,
    UUID incidentId,
    IncidentStatus status,
    DeduplicationAction deduplicationAction) {}
