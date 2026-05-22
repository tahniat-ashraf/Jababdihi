package com.jababdihi.backend.incident.processing;

import java.util.Collection;
import java.util.Locale;

class KeywordMatcher {
  boolean containsAny(String text, Collection<String> keywords) {
    if (text == null || text.isBlank()) {
      return false;
    }

    String normalizedText = text.toLowerCase(Locale.ROOT);
    return keywords.stream()
        .filter(keyword -> keyword != null && !keyword.isBlank())
        .map(keyword -> keyword.toLowerCase(Locale.ROOT))
        .anyMatch(normalizedText::contains);
  }

  int countMatches(String text, Collection<String> keywords) {
    if (text == null || text.isBlank()) {
      return 0;
    }

    String normalizedText = text.toLowerCase(Locale.ROOT);
    int matches = 0;
    for (String keyword : keywords) {
      if (keyword != null
          && !keyword.isBlank()
          && normalizedText.contains(keyword.toLowerCase(Locale.ROOT))) {
        matches++;
      }
    }
    return matches;
  }
}
