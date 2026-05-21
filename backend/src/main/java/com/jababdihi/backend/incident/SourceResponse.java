package com.jababdihi.backend.incident;

import java.time.Instant;

public record SourceResponse(
    String publisherName, String sourceTitle, String url, Instant publishedAt) {}
