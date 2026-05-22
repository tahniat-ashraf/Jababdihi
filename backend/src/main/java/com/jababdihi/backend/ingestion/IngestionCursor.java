package com.jababdihi.backend.ingestion;

import com.jababdihi.backend.source.Publisher;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ingestion_cursors")
public class IngestionCursor {
  @Id @GeneratedValue private UUID id;

  @Enumerated(EnumType.STRING)
  private IngestionJobType jobType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "publisher_id")
  private Publisher publisher;

  private Instant cursorPublishedAt;
  private String lockedBy;
  private Instant lockedAt;
  private Instant heartbeatAt;
  private String lastRunStatus;
  private String lastError;
  private Instant createdAt;
  private Instant updatedAt;

  public UUID getId() {
    return id;
  }

  public IngestionJobType getJobType() {
    return jobType;
  }

  public void setJobType(IngestionJobType jobType) {
    this.jobType = jobType;
  }

  public Publisher getPublisher() {
    return publisher;
  }

  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }

  public Instant getCursorPublishedAt() {
    return cursorPublishedAt;
  }

  public void setCursorPublishedAt(Instant cursorPublishedAt) {
    this.cursorPublishedAt = cursorPublishedAt;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
  }

  public Instant getLockedAt() {
    return lockedAt;
  }

  public void setLockedAt(Instant lockedAt) {
    this.lockedAt = lockedAt;
  }

  public Instant getHeartbeatAt() {
    return heartbeatAt;
  }

  public void setHeartbeatAt(Instant heartbeatAt) {
    this.heartbeatAt = heartbeatAt;
  }

  public String getLastRunStatus() {
    return lastRunStatus;
  }

  public void setLastRunStatus(String lastRunStatus) {
    this.lastRunStatus = lastRunStatus;
  }

  public String getLastError() {
    return lastError;
  }

  public void setLastError(String lastError) {
    this.lastError = lastError;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }
}
