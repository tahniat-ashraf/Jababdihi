package com.jababdihi.backend.ingestion;

public record IngestionResult(int fetchedItems, int storedItems, int skippedItems) {}
