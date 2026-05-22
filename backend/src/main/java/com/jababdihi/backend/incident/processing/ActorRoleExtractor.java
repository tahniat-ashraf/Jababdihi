package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.ActorRole;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class ActorRoleExtractor {
  private final IncidentProcessingProperties properties;
  private final KeywordMatcher keywordMatcher = new KeywordMatcher();

  ActorRoleExtractor(IncidentProcessingProperties properties) {
    this.properties = properties;
  }

  ActorRole extract(String title, String text) {
    String searchableText = title + "\n" + text;
    int governmentMatches =
        keywordMatcher.countMatches(searchableText, properties.getGovernmentActorKeywords());
    int oppositionMatches =
        keywordMatcher.countMatches(searchableText, properties.getOppositionActorKeywords());
    if (governmentMatches > oppositionMatches) {
      return ActorRole.GOVERNMENT;
    }
    if (oppositionMatches > governmentMatches) {
      return ActorRole.OPPOSITION;
    }
    return ActorRole.UNKNOWN;
  }
}
