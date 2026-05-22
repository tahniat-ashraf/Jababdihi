package com.jababdihi.backend.taskqueue;

public enum ProcessingTaskStatus {
  PENDING,
  RUNNING,
  SUCCEEDED,
  FAILED_RETRYABLE,
  FAILED_PERMANENTLY
}
