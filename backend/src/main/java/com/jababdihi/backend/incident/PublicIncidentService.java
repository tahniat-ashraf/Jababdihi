package com.jababdihi.backend.incident;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.CategoryCode;
import com.jababdihi.backend.common.LanguageCode;
import com.jababdihi.backend.common.PageResponse;
import com.jababdihi.backend.common.SortOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PublicIncidentService {
  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int MAX_PAGE_SIZE = 50;

  private final IncidentRepository incidentRepository;
  private final IncidentMapper incidentMapper;

  PublicIncidentService(IncidentRepository incidentRepository, IncidentMapper incidentMapper) {
    this.incidentRepository = incidentRepository;
    this.incidentMapper = incidentMapper;
  }

  @Transactional(readOnly = true)
  public PageResponse<IncidentSummaryResponse> findIncidents(
      String actorRole,
      Collection<String> categories,
      String sort,
      String language,
      Integer page,
      Integer pageSize) {
    LanguageCode languageCode = LanguageCode.fromRequest(language);
    ActorRole actor = parseActorRole(actorRole);
    List<CategoryCode> categoryCodes = parseCategories(categories);
    int requestedPage = normalizePage(page);
    int requestedPageSize = normalizePageSize(pageSize);

    var pageable =
        PageRequest.of(requestedPage - 1, requestedPageSize, sortFor(SortOption.fromRequest(sort)));
    var incidents =
        incidentRepository.findAll(
            IncidentSpecifications.publicFeed(actor, categoryCodes), pageable);

    return new PageResponse<>(
        incidents.map(incident -> incidentMapper.toSummary(incident, languageCode)).getContent(),
        requestedPage,
        requestedPageSize,
        incidents.getTotalPages(),
        incidents.getTotalElements());
  }

  @Transactional(readOnly = true)
  public IncidentDetailResponse findIncident(UUID id, String language) {
    LanguageCode languageCode = LanguageCode.fromRequest(language);
    Incident incident =
        incidentRepository
            .findOne(IncidentSpecifications.publishedDetail(id))
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Incident not found"));
    return incidentMapper.toDetail(incident, languageCode);
  }

  private ActorRole parseActorRole(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      ActorRole actorRole = ActorRole.valueOf(value.toUpperCase(Locale.ROOT));
      if (!actorRole.isPubliclyVisible()) {
        throw new ResponseStatusException(
            BAD_REQUEST, "actorRole must be GOVERNMENT or OPPOSITION");
      }
      return actorRole;
    } catch (IllegalArgumentException exception) {
      throw new ResponseStatusException(BAD_REQUEST, "actorRole must be GOVERNMENT or OPPOSITION");
    }
  }

  private List<CategoryCode> parseCategories(Collection<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }

    List<CategoryCode> categories = new ArrayList<>();
    for (String value : values) {
      if (value == null || value.isBlank()) {
        continue;
      }
      for (String token : value.split(",")) {
        String category = token.trim();
        if (category.isEmpty()) {
          continue;
        }
        try {
          categories.add(CategoryCode.valueOf(category.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
          throw new ResponseStatusException(
              BAD_REQUEST, "category contains an unsupported category code");
        }
      }
    }
    return categories.stream().distinct().toList();
  }

  private int normalizePage(Integer page) {
    if (page == null) {
      return 1;
    }
    if (page < 1) {
      throw new ResponseStatusException(BAD_REQUEST, "page must be greater than or equal to 1");
    }
    return page;
  }

  private int normalizePageSize(Integer pageSize) {
    if (pageSize == null) {
      return DEFAULT_PAGE_SIZE;
    }
    if (pageSize < 1) {
      throw new ResponseStatusException(BAD_REQUEST, "pageSize must be greater than or equal to 1");
    }
    return Math.min(pageSize, MAX_PAGE_SIZE);
  }

  private Sort sortFor(SortOption sort) {
    return switch (sort) {
      case NEWEST -> Sort.by(Sort.Order.desc("incidentDate"), Sort.Order.desc("createdAt"));
      case MOST_CORROBORATED ->
          Sort.by(
              Sort.Order.desc("confidenceScore"),
              Sort.Order.desc("independentPublisherCount"),
              Sort.Order.desc("sourceCount"),
              Sort.Order.desc("incidentDate"));
      case RECOMMENDED ->
          Sort.by(
              Sort.Order.desc("confidenceScore"),
              Sort.Order.desc("incidentDate"),
              Sort.Order.desc("createdAt"));
    };
  }
}
