package com.jababdihi.backend.ingestion;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ingestion")
public class IngestionProperties {
  @Valid private List<PublisherFeedProperties> publishers = new ArrayList<>();

  public List<PublisherFeedProperties> getPublishers() {
    return publishers;
  }

  public void setPublishers(List<PublisherFeedProperties> publishers) {
    this.publishers = publishers;
  }

  public static class PublisherFeedProperties {
    @NotBlank private String name;
    @NotBlank private String domain;
    @NotBlank private String homepageUrl;
    @NotBlank private String feedUrl;
    @NotBlank private String languageCode;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getDomain() {
      return domain;
    }

    public void setDomain(String domain) {
      this.domain = domain;
    }

    public String getHomepageUrl() {
      return homepageUrl;
    }

    public void setHomepageUrl(String homepageUrl) {
      this.homepageUrl = homepageUrl;
    }

    public String getFeedUrl() {
      return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
      this.feedUrl = feedUrl;
    }

    public String getLanguageCode() {
      return languageCode;
    }

    public void setLanguageCode(String languageCode) {
      this.languageCode = languageCode;
    }
  }
}
