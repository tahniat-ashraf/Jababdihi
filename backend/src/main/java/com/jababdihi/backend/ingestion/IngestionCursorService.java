package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("worker")
public class IngestionCursorService {
  private static final Duration STALE_LOCK_AFTER = Duration.ofMinutes(75);
  private static final String STATUS_FAILED = "FAILED";
  private static final String STATUS_SUCCEEDED = "SUCCEEDED";

  private final IngestionCursorRepository cursorRepository;
  private final Clock clock;

  IngestionCursorService(IngestionCursorRepository cursorRepository, Clock clock) {
    this.cursorRepository = cursorRepository;
    this.clock = clock;
  }

  @Transactional
  public Optional<IngestionCursor> claim(
      IngestionJobType jobType, Publisher publisher, String workerId, Instant initialCursor) {
    IngestionCursor cursor = findOrCreate(jobType, publisher, initialCursor);
    Instant now = Instant.now(clock);
    Instant staleBefore = now.minus(STALE_LOCK_AFTER);
    int claimed = cursorRepository.claim(cursor.getId(), workerId, now, staleBefore);
    if (claimed == 0) {
      return Optional.empty();
    }
    return cursorRepository.findById(cursor.getId());
  }

  @Transactional
  public void releaseSucceeded(IngestionCursor cursor, Instant cursorPublishedAt) {
    IngestionCursor managed = cursorRepository.findById(cursor.getId()).orElseThrow();
    managed.setCursorPublishedAt(cursorPublishedAt);
    managed.setLockedBy(null);
    managed.setLockedAt(null);
    managed.setHeartbeatAt(null);
    managed.setLastRunStatus(STATUS_SUCCEEDED);
    managed.setLastError(null);
    managed.setUpdatedAt(Instant.now(clock));
  }

  @Transactional
  public void releaseFailed(IngestionCursor cursor, RuntimeException failure) {
    IngestionCursor managed = cursorRepository.findById(cursor.getId()).orElseThrow();
    managed.setLockedBy(null);
    managed.setLockedAt(null);
    managed.setHeartbeatAt(null);
    managed.setLastRunStatus(STATUS_FAILED);
    managed.setLastError(failure.getMessage());
    managed.setUpdatedAt(Instant.now(clock));
  }

  private IngestionCursor findOrCreate(
      IngestionJobType jobType, Publisher publisher, Instant initialCursor) {
    return cursorRepository
        .findByJobTypeAndPublisher(jobType, publisher)
        .orElseGet(() -> cursorRepository.save(newCursor(jobType, publisher, initialCursor)));
  }

  private IngestionCursor newCursor(
      IngestionJobType jobType, Publisher publisher, Instant initialCursor) {
    Instant now = Instant.now(clock);
    IngestionCursor cursor = new IngestionCursor();
    cursor.setJobType(jobType);
    cursor.setPublisher(publisher);
    cursor.setCursorPublishedAt(initialCursor);
    cursor.setCreatedAt(now);
    cursor.setUpdatedAt(now);
    return cursor;
  }
}
