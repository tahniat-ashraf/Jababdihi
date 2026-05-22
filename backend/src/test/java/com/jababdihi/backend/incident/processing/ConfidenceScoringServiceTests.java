package com.jababdihi.backend.incident.processing;

import static org.assertj.core.api.Assertions.assertThat;

import com.jababdihi.backend.incident.Category;
import com.jababdihi.backend.incident.Incident;
import com.jababdihi.backend.location.Location;
import com.jababdihi.backend.source.IncidentSource;
import com.jababdihi.backend.source.Publisher;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ConfidenceScoringServiceTests {
  private final ConfidenceScoringService service = new ConfidenceScoringService();

  @Test
  void scoreUsesSourcePublisherAndMetadataWeights() {
    Incident incident = new Incident();
    incident.setIncidentDate(LocalDate.of(2026, 5, 22));
    Location location = new Location();
    location.setDistrict("Dhaka");
    incident.setLocation(location);
    Category category = new Category();
    category.setCode("EXTORTION");
    incident.getCategories().add(category);
    incident.getSources().add(source("prothomalo.com"));
    incident.getSources().add(source("thedailystar.net"));
    incident.setSourceCount(2);
    incident.setIndependentPublisherCount(2);

    BigDecimal score = service.score(incident);

    assertThat(score).isEqualByComparingTo(BigDecimal.valueOf(59.50));
    assertThat(service.confidenceLevel(score)).isEqualTo("MODERATE");
  }

  private IncidentSource source(String domain) {
    Publisher publisher = new Publisher();
    publisher.setDomain(domain);
    IncidentSource source = new IncidentSource();
    source.setPublisher(publisher);
    source.setPublishedAt(Instant.parse("2026-05-22T08:00:00Z"));
    return source;
  }
}
