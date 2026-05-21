package com.jababdihi.backend.common;

public enum ActorRole {
  GOVERNMENT("Government", "সরকার", "blue"),
  OPPOSITION("Opposition", "বিরোধী দল", "red"),
  UNKNOWN("Unknown", "অজানা", "gray");

  private final String labelEn;
  private final String labelBn;
  private final String color;

  ActorRole(String labelEn, String labelBn, String color) {
    this.labelEn = labelEn;
    this.labelBn = labelBn;
    this.color = color;
  }

  public String label(LanguageCode language) {
    return language == LanguageCode.BN ? labelBn : labelEn;
  }

  public String color() {
    return color;
  }

  public boolean isPubliclyVisible() {
    return this == GOVERNMENT || this == OPPOSITION;
  }
}
