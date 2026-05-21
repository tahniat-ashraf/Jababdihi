package com.jababdihi.backend.common;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

import org.springframework.web.server.ResponseStatusException;

public enum SortOption {
  RECOMMENDED,
  NEWEST,
  MOST_CORROBORATED;

  public static SortOption fromRequest(String value) {
    if (value == null || value.isBlank()) {
      return RECOMMENDED;
    }
    try {
      return valueOf(value.toUpperCase());
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(
          BAD_REQUEST, "sort must be RECOMMENDED, NEWEST, or MOST_CORROBORATED");
    }
  }
}
