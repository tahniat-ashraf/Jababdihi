package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.LanguageCode;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.source.IncidentSource;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class IncidentMapper {
  private static final String DISCLAIMER_EN =
      "Confidence scores represent source corroboration strength, not legal proof, guilt, or a"
          + " court finding.";
  private static final String DISCLAIMER_BN =
      "আস্থা স্কোর উৎস-সমর্থনের শক্তি বোঝায়; এটি আইনি প্রমাণ, দোষ, বা আদালতের সিদ্ধান্ত নয়।";

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
        confidenceResponse(incident, language),
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
        confidenceResponse(incident, language),
        sortedSources(incident).stream()
            .map(
                source ->
                    new SourceResponse(
                        publisherName(source),
                        source.getSourceTitle(),
                        source.getSourceUrl(),
                        source.getPublishedAt()))
            .toList(),
        disclaimer(language));
  }

  private TranslationText translationFor(Incident incident, LanguageCode language) {
    return preferredTranslation(incident, language)
        .map(translation -> new TranslationText(translation.getTitle(), translation.getSummary()))
        .orElse(new TranslationText("", ""));
  }

  private Optional<IncidentTranslation> preferredTranslation(
      Incident incident, LanguageCode language) {
    return translationWithLanguage(incident, language)
        .or(() -> translationWithLanguage(incident, LanguageCode.BN))
        .or(() -> translationWithLanguage(incident, LanguageCode.EN))
        .or(() -> incident.getTranslations().stream().findFirst());
  }

  private Optional<IncidentTranslation> translationWithLanguage(
      Incident incident, LanguageCode language) {
    return incident.getTranslations().stream()
        .filter(translation -> language.value().equals(translation.getLanguageCode()))
        .filter(
            translation -> firstNonBlank(translation.getTitle(), translation.getSummary()) != null)
        .findFirst();
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

  private ConfidenceResponse confidenceResponse(Incident incident, LanguageCode language) {
    Integer score =
        incident.getConfidenceScore() == null
            ? null
            : incident.getConfidenceScore().setScale(0, RoundingMode.HALF_UP).intValue();
    String label = confidenceLabel(incident.getConfidenceLevel(), score, language);
    String explanation = confidenceExplanation(incident, language);
    return new ConfidenceResponse(score, label, explanation);
  }

  private String confidenceLabel(String storedLabel, Integer score, LanguageCode language) {
    String normalizedLabel = storedLabel == null ? null : storedLabel.toUpperCase(Locale.ROOT);
    if ("HIGH".equals(normalizedLabel) || "HIGHLY_CORROBORATED".equals(normalizedLabel)) {
      return language == LanguageCode.BN ? "উচ্চ সমর্থন" : "Highly Corroborated";
    }
    if ("MODERATE".equals(normalizedLabel) || "MODERATE_CORROBORATION".equals(normalizedLabel)) {
      return language == LanguageCode.BN ? "মধ্যম সমর্থন" : "Moderate Corroboration";
    }
    if ("LOW".equals(normalizedLabel) || "LOW_CORROBORATION".equals(normalizedLabel)) {
      return language == LanguageCode.BN ? "সীমিত সমর্থন" : "Limited Corroboration";
    }
    if (storedLabel != null && !storedLabel.isBlank()) {
      return storedLabel;
    }
    if (score == null) {
      return null;
    }
    if (score >= 75) {
      return language == LanguageCode.BN ? "উচ্চ সমর্থন" : "Highly Corroborated";
    }
    if (score >= 55) {
      return language == LanguageCode.BN ? "সমর্থিত" : "Corroborated";
    }
    return language == LanguageCode.BN ? "সীমিত সমর্থন" : "Limited Corroboration";
  }

  private String confidenceExplanation(Incident incident, LanguageCode language) {
    if (language == LanguageCode.BN) {
      return incident.getSourceCount()
          + "টি উৎস এবং "
          + incident.getIndependentPublisherCount()
          + "টি স্বাধীন প্রকাশকের প্রতিবেদনের ভিত্তিতে।";
    }
    return "Reported by "
        + incident.getSourceCount()
        + " sources from "
        + incident.getIndependentPublisherCount()
        + " independent publishers.";
  }

  private String disclaimer(LanguageCode language) {
    return language == LanguageCode.BN ? DISCLAIMER_BN : DISCLAIMER_EN;
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
