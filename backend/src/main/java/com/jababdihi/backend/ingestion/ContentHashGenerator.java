package com.jababdihi.backend.ingestion;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
class ContentHashGenerator {
  String hash(String title, String extractedText) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      String content = normalize(title) + "\n" + normalize(extractedText);
      return HexFormat.of().formatHex(digest.digest(content.getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException ex) {
      throw new IllegalStateException("SHA-256 is required by the Java runtime", ex);
    }
  }

  private String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.trim().replaceAll("\\s+", " ");
  }
}
