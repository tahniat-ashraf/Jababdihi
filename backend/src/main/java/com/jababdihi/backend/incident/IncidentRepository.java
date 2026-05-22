package com.jababdihi.backend.incident;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IncidentRepository
    extends JpaRepository<Incident, UUID>, JpaSpecificationExecutor<Incident> {
  List<Incident> findTop100ByIncidentDateOrderByCreatedAtDesc(LocalDate incidentDate);

  List<Incident> findTop100ByOrderByCreatedAtDesc();
}
