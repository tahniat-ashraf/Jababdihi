package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.CategoryCode;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class KeywordCategoryAssignmentService {
  private final IncidentProcessingProperties properties;
  private final KeywordMatcher keywordMatcher = new KeywordMatcher();

  KeywordCategoryAssignmentService(IncidentProcessingProperties properties) {
    this.properties = properties;
  }

  CategoryAssignment assign(String title, String text) {
    String searchableText = title + "\n" + text;
    for (IncidentProcessingProperties.CategoryRule rule : properties.getCategoryRules()) {
      if (keywordMatcher.containsAny(searchableText, rule.getKeywords())) {
        return new CategoryAssignment(CategoryCode.valueOf(rule.getCode()), true);
      }
    }

    return new CategoryAssignment(
        CategoryCode.valueOf(properties.getFallbackCategoryCode()), false);
  }
}
