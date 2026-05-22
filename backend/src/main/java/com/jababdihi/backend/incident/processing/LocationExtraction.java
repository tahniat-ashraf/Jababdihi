package com.jababdihi.backend.incident.processing;

import java.util.Optional;

public record LocationExtraction(Optional<String> district, String extractedText) {}
