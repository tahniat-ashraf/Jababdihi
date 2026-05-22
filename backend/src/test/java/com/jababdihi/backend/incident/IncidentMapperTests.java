package com.jababdihi.backend.incident;

import static org.assertj.core.api.Assertions.assertThat;

import com.jababdihi.backend.common.ActorRole;
import com.jababdihi.backend.common.IncidentStatus;
import com.jababdihi.backend.common.LanguageCode;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class IncidentMapperTests {
  private final IncidentMapper mapper = new IncidentMapper();

  @Test
  void toSummaryFallsBackToBanglaWhenEnglishTranslationIsMissing() {
    Incident incident = incident();
    incident
        .getTranslations()
        .add(
            new IncidentTranslation(
                incident, "bn", "বাংলা শিরোনাম", "বাংলা সারসংক্ষেপ বাধ্যতামূলক।"));

    IncidentSummaryResponse response = mapper.toSummary(incident, LanguageCode.EN);

    assertThat(response.title()).isEqualTo("বাংলা শিরোনাম");
    assertThat(response.summary()).isEqualTo("বাংলা সারসংক্ষেপ বাধ্যতামূলক।");
    assertThat(response.confidence().label()).isEqualTo("Highly Corroborated");
  }

  @Test
  void toDetailReturnsLocalizedConfidenceExplanationAndDisclaimer() {
    Incident incident = incident();
    incident
        .getTranslations()
        .add(new IncidentTranslation(incident, "bn", "শিরোনাম", "সারসংক্ষেপ"));

    IncidentDetailResponse response = mapper.toDetail(incident, LanguageCode.BN);

    assertThat(response.confidence().label()).isEqualTo("উচ্চ সমর্থন");
    assertThat(response.confidence().explanation()).contains("উৎস");
    assertThat(response.disclaimer()).contains("আইনি প্রমাণ");
  }

  private Incident incident() {
    Incident incident = new Incident();
    ReflectionTestUtils.setField(incident, "id", UUID.randomUUID());
    incident.setActorRole(ActorRole.GOVERNMENT);
    incident.setIncidentDate(LocalDate.of(2026, 3, 18));
    incident.setStatus(IncidentStatus.AUTO_PUBLISHED);
    incident.setConfidenceScore(BigDecimal.valueOf(82));
    incident.setConfidenceLevel("HIGH");
    incident.setSourceCount(2);
    incident.setIndependentPublisherCount(2);
    incident.setCreatedAt(Instant.parse("2026-03-18T10:00:00Z"));
    incident.setUpdatedAt(Instant.parse("2026-03-18T10:00:00Z"));
    return incident;
  }
}
