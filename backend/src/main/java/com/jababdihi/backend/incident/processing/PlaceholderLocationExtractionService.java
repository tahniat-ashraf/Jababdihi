package com.jababdihi.backend.incident.processing;

import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class PlaceholderLocationExtractionService {
  private final IncidentProcessingProperties properties;
  private final KeywordMatcher keywordMatcher = new KeywordMatcher();

  PlaceholderLocationExtractionService(IncidentProcessingProperties properties) {
    this.properties = properties;
  }

  LocationExtraction extract(String title, String text) {
    String searchableText = title + "\n" + text;
    for (String district : properties.getDistrictKeywords()) {
      if (keywordMatcher.containsAny(searchableText, java.util.List.of(district))) {
        return new LocationExtraction(Optional.of(district), district);
      }
    }
    return new LocationExtraction(Optional.empty(), "");
  }
}
