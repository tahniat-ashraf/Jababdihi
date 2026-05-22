package com.jababdihi.backend.ingestion;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public record CurrentPublisherIngestionResult(
    int fetchedItems,
    int attemptedItems,
    int storedItems,
    int skippedItems,
    Optional<Instant> cursorPublishedAt,
    List<UUID> rawContentIds) {}
