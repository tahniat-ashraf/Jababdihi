package com.jababdihi.backend.incident;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "incident_translations")
public class IncidentTranslation {
  @EmbeddedId private IncidentTranslationId id;

  @ManyToOne(fetch = FetchType.LAZY)
  @MapsId("incidentId")
  @JoinColumn(name = "incident_id")
  private Incident incident;

  private String title;
  private String summary;

  protected IncidentTranslation() {}

  public IncidentTranslation(Incident incident, String languageCode, String title, String summary) {
    this.incident = incident;
    this.id = new IncidentTranslationId(incident.getId(), languageCode);
    this.title = title;
    this.summary = summary;
  }

  public IncidentTranslationId getId() {
    return id;
  }

  public void setId(IncidentTranslationId id) {
    this.id = id;
  }

  public Incident getIncident() {
    return incident;
  }

  public void setIncident(Incident incident) {
    this.incident = incident;
  }

  public String getLanguageCode() {
    return id == null ? null : id.getLanguageCode();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }
}
