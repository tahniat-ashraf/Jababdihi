package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.LanguageCode;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.source.IncidentSource;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class IncidentMapper {
  private static final String DISCLAIMER =
      "Confidence scores represent source corroboration strength, not legal proof, guilt, or a"
          + " court finding.";

  IncidentSummaryResponse toSummary(Incident incident, LanguageCode language) {
    var translation = translationFor(incident, language);
    var sources = sortedSources(incident);
    var preview =
        sources.stream()
            .limit(3)
            .map(source -> new SourcePreviewResponse(publisherName(source), source.getSourceUrl()))
            .toList();

    return new IncidentSummaryResponse(
        incident.getId(),
        incident.getActorRole().name(),
        incident.getActorRole().color(),
        translation.title(),
        translation.summary(),
        categoryCodes(incident),
        incident.getIncidentDate(),
        locationResponse(incident),
        confidenceResponse(incident),
        preview,
        Math.max(0, incident.getSourceCount() - preview.size()),
        "/" + language.value() + "/incidents/" + incident.getId());
  }

  IncidentDetailResponse toDetail(Incident incident, LanguageCode language) {
    var translation = translationFor(incident, language);
    return new IncidentDetailResponse(
        incident.getId(),
        incident.getActorRole().name(),
        incident.getActorRole().color(),
        translation.title(),
        translation.summary(),
        categoryCodes(incident),
        incident.getIncidentDate(),
        locationResponse(incident),
        confidenceResponse(incident),
        sortedSources(incident).stream()
            .map(
                source ->
                    new SourceResponse(
                        publisherName(source),
                        source.getSourceTitle(),
                        source.getSourceUrl(),
                        source.getPublishedAt()))
            .toList(),
        DISCLAIMER);
  }

  private TranslationText translationFor(Incident incident, LanguageCode language) {
    return incident.getTranslations().stream()
        .filter(translation -> language.value().equals(translation.getLanguageCode()))
        .findFirst()
        .or(() -> incident.getTranslations().stream().findFirst())
        .map(translation -> new TranslationText(translation.getTitle(), translation.getSummary()))
        .orElse(new TranslationText("", ""));
  }

  private List<String> categoryCodes(Incident incident) {
    return incident.getCategories().stream().map(Category::getCode).sorted().toList();
  }

  private LocationResponse locationResponse(Incident incident) {
    Location location = incident.getLocation();
    if (location == null && incident.getExtractedLocationText() == null) {
      return null;
    }
    if (location == null) {
      return new LocationResponse(null, null, null, null, incident.getExtractedLocationText());
    }
    String displayText =
        firstNonBlank(
            incident.getExtractedLocationText(),
            location.getDistrict(),
            location.getDivision(),
            location.getCountry());
    return new LocationResponse(
        location.getCountry(),
        location.getDivision(),
        location.getDistrict(),
        location.getUpazila(),
        displayText);
  }

  private ConfidenceResponse confidenceResponse(Incident incident) {
    Integer score =
        incident.getConfidenceScore() == null
            ? null
            : incident.getConfidenceScore().setScale(0, RoundingMode.HALF_UP).intValue();
    String label = firstNonBlank(incident.getConfidenceLevel(), confidenceLabel(score));
    String explanation =
        "Reported by "
            + incident.getSourceCount()
            + " sources from "
            + incident.getIndependentPublisherCount()
            + " independent publishers.";
    return new ConfidenceResponse(score, label, explanation);
  }

  private String confidenceLabel(Integer score) {
    if (score == null) {
      return null;
    }
    if (score >= 75) {
      return "Highly Corroborated";
    }
    if (score >= 55) {
      return "Corroborated";
    }
    return "Limited Corroboration";
  }

  private List<IncidentSource> sortedSources(Incident incident) {
    return incident.getSources().stream()
        .sorted(Comparator.comparing(IncidentMapper::publishedAtOrEpoch).reversed())
        .toList();
  }

  private static Instant publishedAtOrEpoch(IncidentSource source) {
    return source.getPublishedAt() == null ? Instant.EPOCH : source.getPublishedAt();
  }

  private String publisherName(IncidentSource source) {
    return source.getPublisher() == null ? null : source.getPublisher().getName();
  }

  private String firstNonBlank(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return null;
  }

  private record TranslationText(String title, String summary) {}
}
