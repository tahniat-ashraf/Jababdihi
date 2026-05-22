package com.jababdihi.backend.taskqueue;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface ProcessingTaskRepository extends JpaRepository<ProcessingTask, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      select task
      from ProcessingTask task
      where task.status = :status
        and task.availableAt <= :now
      order by task.availableAt asc, task.createdAt asc
      """)
  List<ProcessingTask> findDueTasksForUpdate(
      ProcessingTaskStatus status, Instant now, Pageable pageable);

  long countByStatus(ProcessingTaskStatus status);
}
