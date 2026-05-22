package com.jababdihi.backend.admin;

import com.jababdihi.backend.common.PageResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/admin")
public class AdminIncidentController {
  private final AdminIncidentService adminIncidentService;

  AdminIncidentController(AdminIncidentService adminIncidentService) {
    this.adminIncidentService = adminIncidentService;
  }

  @GetMapping("/incidents/pending")
  public PageResponse<AdminPendingIncident> getPending(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return adminIncidentService.findPending(page, pageSize);
  }

  @GetMapping("/incidents/{id}")
  public AdminIncidentDetail getDetail(@PathVariable UUID id) {
    return adminIncidentService.findById(id);
  }

  @PostMapping("/incidents/{id}/publish")
  public AdminActionResponse publish(
      @PathVariable UUID id, @RequestBody(required = false) AdminActionRequest body) {
    return adminIncidentService.publish(id);
  }

  @PostMapping("/incidents/{id}/reject")
  public AdminActionResponse reject(
      @PathVariable UUID id, @RequestBody(required = false) AdminActionRequest body) {
    return adminIncidentService.reject(id);
  }

  @PostMapping("/incidents/{id}/archive")
  public AdminActionResponse archive(
      @PathVariable UUID id, @RequestBody(required = false) AdminActionRequest body) {
    return adminIncidentService.archive(id);
  }

  @PostMapping("/incidents/{id}/reprocess")
  public AdminActionResponse reprocess(
      @PathVariable UUID id, @RequestBody(required = false) AdminActionRequest body) {
    return adminIncidentService.reprocess(id);
  }
}
