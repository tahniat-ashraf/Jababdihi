package com.jababdihi.backend.taskqueue;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, UUID> {}
