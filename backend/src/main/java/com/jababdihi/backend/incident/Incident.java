package com.jababdihi.backend.incident;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.IncidentStatus;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.source.IncidentSource;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "incidents")
public class Incident {
  @Id @GeneratedValue private UUID id;

  @Enumerated(EnumType.STRING)
  private ActorRole actorRole;

  private LocalDate incidentDate;

  @ManyToOne
  @JoinColumn(name = "location_id")
  private Location location;

  private String extractedLocationText;
  private BigDecimal confidenceScore;
  private String confidenceLevel;

  @Enumerated(EnumType.STRING)
  private IncidentStatus status;

  private int sourceCount;
  private int independentPublisherCount;
  private Instant createdAt;
  private Instant updatedAt;

  @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<IncidentTranslation> translations = new HashSet<>();

  @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<IncidentSource> sources = new HashSet<>();

  @ManyToMany
  @JoinTable(
      name = "incident_categories",
      joinColumns = @JoinColumn(name = "incident_id"),
      inverseJoinColumns = @JoinColumn(name = "category_code"))
  private Set<Category> categories = new HashSet<>();

  protected Incident() {}

  public UUID getId() {
    return id;
  }

  public ActorRole getActorRole() {
    return actorRole;
  }

  public void setActorRole(ActorRole actorRole) {
    this.actorRole = actorRole;
  }

  public LocalDate getIncidentDate() {
    return incidentDate;
  }

  public void setIncidentDate(LocalDate incidentDate) {
    this.incidentDate = incidentDate;
  }

  public Location getLocation() {
    return location;
  }

  public void setLocation(Location location) {
    this.location = location;
  }

  public String getExtractedLocationText() {
    return extractedLocationText;
  }

  public void setExtractedLocationText(String extractedLocationText) {
    this.extractedLocationText = extractedLocationText;
  }

  public BigDecimal getConfidenceScore() {
    return confidenceScore;
  }

  public void setConfidenceScore(BigDecimal confidenceScore) {
    this.confidenceScore = confidenceScore;
  }

  public String getConfidenceLevel() {
    return confidenceLevel;
  }

  public void setConfidenceLevel(String confidenceLevel) {
    this.confidenceLevel = confidenceLevel;
  }

  public IncidentStatus getStatus() {
    return status;
  }

  public void setStatus(IncidentStatus status) {
    this.status = status;
  }

  public int getSourceCount() {
    return sourceCount;
  }

  public void setSourceCount(int sourceCount) {
    this.sourceCount = sourceCount;
  }

  public int getIndependentPublisherCount() {
    return independentPublisherCount;
  }

  public void setIndependentPublisherCount(int independentPublisherCount) {
    this.independentPublisherCount = independentPublisherCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Set<IncidentTranslation> getTranslations() {
    return translations;
  }

  public Set<IncidentSource> getSources() {
    return sources;
  }

  public Set<Category> getCategories() {
    return categories;
  }
}
