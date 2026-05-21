package com.jababdihi.backend.common;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.server.ResponseStatusException;

public enum LanguageCode {
  BN("bn"),
  EN("en");

  private final String value;

  LanguageCode(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }

  public static LanguageCode fromRequest(String value) {
    if (value == null || value.isBlank()) {
      return BN;
    }
    for (LanguageCode language : values()) {
      if (language.value.equalsIgnoreCase(value)) {
        return language;
      }
    }
    throw new ResponseStatusException(BAD_REQUEST, "language must be bn or en");
  }
}
