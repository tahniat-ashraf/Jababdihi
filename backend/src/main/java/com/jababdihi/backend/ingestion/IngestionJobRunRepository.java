package com.jababdihi.backend.ingestion;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionJobRunRepository extends JpaRepository<IngestionJobRun, UUID> {}
