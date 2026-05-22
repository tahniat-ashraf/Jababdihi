package com.jababdihi.backend.incident.processing;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.incident-processing")
public class IncidentProcessingProperties {
  @Valid private Deduplication deduplication = new Deduplication();
  @Valid private List<CategoryRule> categoryRules = new ArrayList<>();
  private String fallbackCategoryCode = "ABUSE_OF_POWER";
  private List<String> politicalKeywords = new ArrayList<>();
  private List<String> accountabilityKeywords = new ArrayList<>();
  private List<String> governmentActorKeywords = new ArrayList<>();
  private List<String> oppositionActorKeywords = new ArrayList<>();
  private List<String> districtKeywords = new ArrayList<>();

  public Deduplication getDeduplication() {
    return deduplication;
  }

  public void setDeduplication(Deduplication deduplication) {
    this.deduplication = deduplication;
  }

  public List<CategoryRule> getCategoryRules() {
    return categoryRules;
  }

  public void setCategoryRules(List<CategoryRule> categoryRules) {
    this.categoryRules = categoryRules;
  }

  public String getFallbackCategoryCode() {
    return fallbackCategoryCode;
  }

  public void setFallbackCategoryCode(String fallbackCategoryCode) {
    this.fallbackCategoryCode = fallbackCategoryCode;
  }

  public List<String> getPoliticalKeywords() {
    return politicalKeywords;
  }

  public void setPoliticalKeywords(List<String> politicalKeywords) {
    this.politicalKeywords = politicalKeywords;
  }

  public List<String> getAccountabilityKeywords() {
    return accountabilityKeywords;
  }

  public void setAccountabilityKeywords(List<String> accountabilityKeywords) {
    this.accountabilityKeywords = accountabilityKeywords;
  }

  public List<String> getGovernmentActorKeywords() {
    return governmentActorKeywords;
  }

  public void setGovernmentActorKeywords(List<String> governmentActorKeywords) {
    this.governmentActorKeywords = governmentActorKeywords;
  }

  public List<String> getOppositionActorKeywords() {
    return oppositionActorKeywords;
  }

  public void setOppositionActorKeywords(List<String> oppositionActorKeywords) {
    this.oppositionActorKeywords = oppositionActorKeywords;
  }

  public List<String> getDistrictKeywords() {
    return districtKeywords;
  }

  public void setDistrictKeywords(List<String> districtKeywords) {
    this.districtKeywords = districtKeywords;
  }

  public static class Deduplication {
    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double autoMergeThreshold = 0.86;

    @DecimalMin("0.0")
    @DecimalMax("1.0")
    private double manualReviewThreshold = 0.72;

    public double getAutoMergeThreshold() {
      return autoMergeThreshold;
    }

    public void setAutoMergeThreshold(double autoMergeThreshold) {
      this.autoMergeThreshold = autoMergeThreshold;
    }

    public double getManualReviewThreshold() {
      return manualReviewThreshold;
    }

    public void setManualReviewThreshold(double manualReviewThreshold) {
      this.manualReviewThreshold = manualReviewThreshold;
    }
  }

  public static class CategoryRule {
    @NotBlank private String code;
    private List<String> keywords = new ArrayList<>();

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public List<String> getKeywords() {
      return keywords;
    }

    public void setKeywords(List<String> keywords) {
      this.keywords = keywords;
    }
  }
}
