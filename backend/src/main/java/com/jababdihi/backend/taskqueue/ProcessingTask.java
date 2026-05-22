package com.jababdihi.backend.taskqueue;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "processing_tasks")
public class ProcessingTask {
  @Id @GeneratedValue private UUID id;

  @Enumerated(EnumType.STRING)
  private ProcessingTaskType taskType;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  private String payload;

  @Enumerated(EnumType.STRING)
  private ProcessingTaskStatus status;

  private int attemptCount;
  private int maxAttempts;
  private Instant availableAt;
  private Instant lockedAt;
  private String lockedBy;
  private String lastError;
  private Instant createdAt;
  private Instant updatedAt;

  protected ProcessingTask() {}

  public ProcessingTask(ProcessingTaskType taskType, String payload, Instant availableAt) {
    this.taskType = taskType;
    this.payload = payload;
    this.status = ProcessingTaskStatus.PENDING;
    this.maxAttempts = TaskBackoffPolicy.DEFAULT_MAX_ATTEMPTS;
    this.availableAt = availableAt;
  }

  @PrePersist
  void prePersist() {
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    if (updatedAt == null) {
      updatedAt = now;
    }
    if (status == null) {
      status = ProcessingTaskStatus.PENDING;
    }
    if (maxAttempts == 0) {
      maxAttempts = TaskBackoffPolicy.DEFAULT_MAX_ATTEMPTS;
    }
  }

  @PreUpdate
  void preUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public ProcessingTaskType getTaskType() {
    return taskType;
  }

  public void setTaskType(ProcessingTaskType taskType) {
    this.taskType = taskType;
  }

  public String getPayload() {
    return payload;
  }

  public void setPayload(String payload) {
    this.payload = payload;
  }

  public ProcessingTaskStatus getStatus() {
    return status;
  }

  public void setStatus(ProcessingTaskStatus status) {
    this.status = status;
  }

  public int getAttemptCount() {
    return attemptCount;
  }

  public void setAttemptCount(int attemptCount) {
    this.attemptCount = attemptCount;
  }

  public int getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(int maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public Instant getAvailableAt() {
    return availableAt;
  }

  public void setAvailableAt(Instant availableAt) {
    this.availableAt = availableAt;
  }

  public Instant getLockedAt() {
    return lockedAt;
  }

  public void setLockedAt(Instant lockedAt) {
    this.lockedAt = lockedAt;
  }

  public String getLockedBy() {
    return lockedBy;
  }

  public void setLockedBy(String lockedBy) {
    this.lockedBy = lockedBy;
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
