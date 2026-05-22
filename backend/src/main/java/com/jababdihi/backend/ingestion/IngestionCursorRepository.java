package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface IngestionCursorRepository extends JpaRepository<IngestionCursor, UUID> {
  Optional<IngestionCursor> findByJobTypeAndPublisher(
      IngestionJobType jobType, Publisher publisher);

  @Modifying
  @Query(
      """
      update IngestionCursor cursor
      set cursor.lockedBy = :workerId,
          cursor.lockedAt = :now,
          cursor.heartbeatAt = :now,
          cursor.updatedAt = :now
      where cursor.id = :id
        and (cursor.lockedAt is null or cursor.heartbeatAt < :staleBefore)
      """)
  int claim(UUID id, String workerId, Instant now, Instant staleBefore);
}
