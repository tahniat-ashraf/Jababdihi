package com.jababdihi.backend.source;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentSourceRepository extends JpaRepository<IncidentSource, UUID> {}
