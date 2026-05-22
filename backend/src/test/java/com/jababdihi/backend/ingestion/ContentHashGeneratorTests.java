package com.jababdihi.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ContentHashGeneratorTests {
  private final ContentHashGenerator generator = new ContentHashGenerator();

  @Test
  void hashNormalizesWhitespace() {
    assertThat(generator.hash("Title", "Body text"))
        .isEqualTo(generator.hash(" Title ", "Body   text"));
  }
}
