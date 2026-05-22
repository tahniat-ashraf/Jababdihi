package com.jababdihi.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CanonicalUrlNormalizerTests {
  private final CanonicalUrlNormalizer normalizer = new CanonicalUrlNormalizer();

  @Test
  void normalizeRemovesTrackingParametersAndFragments() {
    assertThat(
            normalizer.normalize(
                "https://www.prothomalo.com/bangladesh/story/?utm_source=feed&fbclid=abc&id=7#top",
                "prothomalo.com"))
        .contains("https://www.prothomalo.com/bangladesh/story?id=7");
  }

  @Test
  void normalizeRejectsUnknownOrSocialDomains() {
    assertThat(normalizer.normalize("https://facebook.com/share/1", "prothomalo.com")).isEmpty();
    assertThat(normalizer.normalize("https://youtube.com/watch?v=1", "prothomalo.com")).isEmpty();
    assertThat(normalizer.normalize("https://example.com/news", "prothomalo.com")).isEmpty();
  }

  @Test
  void normalizeSortsRemainingQueryParameters() {
    assertThat(
            normalizer.normalize(
                "https://bdnews24.com/a?z=2&a=1&utm_medium=social", "bdnews24.com"))
        .contains("https://bdnews24.com/a?a=1&z=2");
  }
}
