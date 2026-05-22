package com.jababdihi.backend.admin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminSourceDetail(
    UUID id,
    String publisherName,
    String sourceTitle,
    String sourceUrl,
    String relevantExcerpt,
    BigDecimal aiRelevanceScore,
    Instant publishedAt) {}
