package com.jababdihi.backend.admin;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.jababdihi.backend.common.IncidentStatus;
import com.jababdihi.backend.common.PageResponse;
import com.jababdihi.backend.incident.Category;
import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.incident.IncidentRepository;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.source.IncidentSource;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminIncidentService {
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 100;

  private final IncidentRepository incidentRepository;

  AdminIncidentService(IncidentRepository incidentRepository) {
    this.incidentRepository = incidentRepository;
  }

  @Transactional(readOnly = true)
  public PageResponse<AdminPendingIncident> findPending(Integer page, Integer pageSize) {
    int p = page == null || page < 1 ? 1 : page;
    int ps = pageSize == null ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
    var pageable = PageRequest.of(p - 1, ps, Sort.by(Sort.Order.desc("createdAt")));
    Specification<Incident> spec =
        (root, query, builder) -> builder.equal(root.get("status"), IncidentStatus.PENDING_REVIEW);
    Page<Incident> result = incidentRepository.findAll(spec, pageable);
    return new PageResponse<>(
        result.map(this::toPendingIncident).getContent(),
        p,
        ps,
        result.getTotalPages(),
        result.getTotalElements());
  }

  @Transactional(readOnly = true)
  public AdminIncidentDetail findById(UUID id) {
    Incident incident =
        incidentRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Incident not found"));
    return toDetail(incident);
  }

  @Transactional
  public AdminActionResponse publish(UUID id) {
    return transitionTo(id, IncidentStatus.MANUALLY_PUBLISHED);
  }

  @Transactional
  public AdminActionResponse reject(UUID id) {
    return transitionTo(id, IncidentStatus.REJECTED);
  }

  @Transactional
  public AdminActionResponse archive(UUID id) {
    return transitionTo(id, IncidentStatus.ARCHIVED);
  }

  @Transactional
  public AdminActionResponse reprocess(UUID id) {
    return transitionTo(id, IncidentStatus.AI_EXTRACTED);
  }

  private AdminActionResponse transitionTo(UUID id, IncidentStatus target) {
    Incident incident =
        incidentRepository
            .findById(id)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Incident not found"));
    incident.setStatus(target);
    incident.setUpdatedAt(Instant.now());
    incidentRepository.save(incident);
    return new AdminActionResponse(id, target.name());
  }

  private AdminPendingIncident toPendingIncident(Incident incident) {
    var translation = preferredTranslation(incident);
    return new AdminPendingIncident(
        incident.getId(),
        incident.getStatus().name(),
        incident.getActorRole().name(),
        translation.title(),
        translation.summary(),
        incident.getSourceCount(),
        incident.getIndependentPublisherCount(),
        roundedScore(incident),
        incident.getConfidenceLevel(),
        incident.getIncidentDate(),
        incident.getCreatedAt());
  }

  private AdminIncidentDetail toDetail(Incident incident) {
    var translation = preferredTranslation(incident);
    Location location = incident.getLocation();
    List<AdminSourceDetail> sources =
        incident.getSources().stream()
            .sorted(Comparator.comparing(AdminIncidentService::publishedAtOrEpoch).reversed())
            .map(this::toSourceDetail)
            .toList();
    List<String> categories =
        incident.getCategories().stream().map(Category::getCode).sorted().toList();
    return new AdminIncidentDetail(
        incident.getId(),
        incident.getStatus().name(),
        incident.getActorRole().name(),
        translation.title(),
        translation.summary(),
        categories,
        location == null ? null : location.getDistrict(),
        location == null ? null : location.getDivision(),
        incident.getSourceCount(),
        incident.getIndependentPublisherCount(),
        roundedScore(incident),
        incident.getConfidenceLevel(),
        incident.getIncidentDate(),
        incident.getCreatedAt(),
        sources);
  }

  private AdminSourceDetail toSourceDetail(IncidentSource source) {
    return new AdminSourceDetail(
        source.getId(),
        source.getPublisher() == null ? null : source.getPublisher().getName(),
        source.getSourceTitle(),
        source.getSourceUrl(),
        source.getRelevantExcerpt(),
        source.getAiRelevanceScore(),
        source.getPublishedAt());
  }

  private record TranslationText(String title, String summary) {}

  private TranslationText preferredTranslation(Incident incident) {
    return incident.getTranslations().stream()
        .filter(t -> "en".equals(t.getLanguageCode()))
        .findFirst()
        .or(() -> incident.getTranslations().stream().findFirst())
        .map(t -> new TranslationText(t.getTitle(), t.getSummary()))
        .orElse(new TranslationText("(no translation)", ""));
  }

  private Integer roundedScore(Incident incident) {
    if (incident.getConfidenceScore() == null) return null;
    return incident.getConfidenceScore().setScale(0, RoundingMode.HALF_UP).intValue();
  }

  private static Instant publishedAtOrEpoch(IncidentSource source) {
    return source.getPublishedAt() == null ? Instant.EPOCH : source.getPublishedAt();
  }
}
