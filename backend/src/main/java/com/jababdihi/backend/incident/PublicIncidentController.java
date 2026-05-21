package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.LanguageCode;
import com.jababdihi.backend.common.PageResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PublicIncidentController {
  private final PublicIncidentService incidentService;
  private final PublicReferenceService referenceService;

  PublicIncidentController(
      PublicIncidentService incidentService, PublicReferenceService referenceService) {
    this.incidentService = incidentService;
    this.referenceService = referenceService;
  }

  @GetMapping("/incidents")
  public PageResponse<IncidentSummaryResponse> incidents(
      @RequestParam(required = false) String actorRole,
      @RequestParam(required = false) List<String> category,
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return incidentService.findIncidents(actorRole, category, sort, language, page, pageSize);
  }

  @GetMapping("/incidents/{id}")
  public IncidentDetailResponse incident(
      @PathVariable UUID id, @RequestParam(required = false) String language) {
    return incidentService.findIncident(id, language);
  }

  @GetMapping("/categories")
  public CategoriesResponse categories(@RequestParam(required = false) String language) {
    return referenceService.categories(LanguageCode.fromRequest(language));
  }

  @GetMapping("/actors")
  public ActorsResponse actors(@RequestParam(required = false) String language) {
    return referenceService.actors(LanguageCode.fromRequest(language));
  }
}
