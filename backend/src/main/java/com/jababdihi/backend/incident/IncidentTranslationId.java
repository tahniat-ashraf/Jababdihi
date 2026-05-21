package com.jababdihi.backend.incident;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class IncidentTranslationId implements Serializable {
  @Column(name = "incident_id")
  private UUID incidentId;

  @Column(name = "language_code")
  private String languageCode;

  protected IncidentTranslationId() {}

  public IncidentTranslationId(UUID incidentId, String languageCode) {
    this.incidentId = incidentId;
    this.languageCode = languageCode;
  }

  public UUID getIncidentId() {
    return incidentId;
  }

  public String getLanguageCode() {
    return languageCode;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof IncidentTranslationId that)) {
      return false;
    }
    return Objects.equals(incidentId, that.incidentId)
        && Objects.equals(languageCode, that.languageCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(incidentId, languageCode);
  }
}
