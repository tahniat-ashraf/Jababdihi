package com.jababdihi.backend.ingestion;

public record CurrentIngestionRunResult(
    int fetchedItems,
    int attemptedItems,
    int storedItems,
    int skippedItems,
    int processedRawContents,
    int failedPublishers,
    int skippedLockedPublishers) {}
