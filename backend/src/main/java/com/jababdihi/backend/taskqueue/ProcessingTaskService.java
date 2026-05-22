package com.jababdihi.backend.taskqueue;

import com.jababdihi.backend.observability.OperationalMetricsService;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProcessingTaskService {
  private final ProcessingTaskRepository taskRepository;
  private final TaskBackoffPolicy backoffPolicy;
  private final Clock clock;
  private final OperationalMetricsService metricsService;

  ProcessingTaskService(
      ProcessingTaskRepository taskRepository,
      TaskBackoffPolicy backoffPolicy,
      Clock clock,
      OperationalMetricsService metricsService) {
    this.taskRepository = taskRepository;
    this.backoffPolicy = backoffPolicy;
    this.clock = clock;
    this.metricsService = metricsService;
  }

  @Transactional
  public ProcessingTask enqueue(ProcessingTaskType taskType, String payload) {
    return enqueue(taskType, payload, Instant.now(clock));
  }

  @Transactional
  public ProcessingTask enqueue(ProcessingTaskType taskType, String payload, Instant availableAt) {
    return taskRepository.save(new ProcessingTask(taskType, payload, availableAt));
  }

  @Transactional
  public List<ProcessingTask> enqueueAll(List<ProcessingTaskType> taskTypes, String payload) {
    Instant now = Instant.now(clock);
    return taskTypes.stream().map(taskType -> enqueue(taskType, payload, now)).toList();
  }

  @Transactional
  public Optional<ProcessingTask> claimNextAvailable(String workerId) {
    Instant now = Instant.now(clock);
    List<ProcessingTask> dueTasks =
        taskRepository.findDueTasksForUpdate(
            ProcessingTaskStatus.PENDING, now, PageRequest.of(0, 1));
    if (dueTasks.isEmpty()) {
      return Optional.empty();
    }

    ProcessingTask task = dueTasks.getFirst();
    task.setStatus(ProcessingTaskStatus.RUNNING);
    task.setAttemptCount(task.getAttemptCount() + 1);
    task.setLockedAt(now);
    task.setLockedBy(workerId);

    return Optional.of(task);
  }

  @Transactional
  public void markSucceeded(UUID taskId) {
    ProcessingTask task = getTask(taskId);
    recordTaskDuration(task);
    task.setStatus(ProcessingTaskStatus.SUCCEEDED);
    task.setLockedAt(null);
    task.setLockedBy(null);
    task.setLastError(null);
  }

  @Transactional
  public ProcessingTask markFailed(UUID taskId, String errorMessage) {
    ProcessingTask task = getTask(taskId);
    recordTaskDuration(task);
    task.setLastError(errorMessage);
    task.setLockedAt(null);
    task.setLockedBy(null);

    if (task.getAttemptCount() >= task.getMaxAttempts()) {
      task.setStatus(ProcessingTaskStatus.FAILED_PERMANENTLY);
      return task;
    }

    task.setStatus(ProcessingTaskStatus.FAILED_RETRYABLE);
    task.setAvailableAt(Instant.now(clock).plus(backoffPolicy.nextDelay(task.getAttemptCount())));
    return task;
  }

  @Transactional
  public int requeueDueRetryableTasks() {
    Instant now = Instant.now(clock);
    List<ProcessingTask> dueTasks =
        taskRepository.findDueTasksForUpdate(
            ProcessingTaskStatus.FAILED_RETRYABLE, now, PageRequest.of(0, 100));
    dueTasks.forEach(
        task -> {
          task.setStatus(ProcessingTaskStatus.PENDING);
          task.setLockedAt(null);
          task.setLockedBy(null);
        });
    return dueTasks.size();
  }

  private ProcessingTask getTask(UUID taskId) {
    return taskRepository
        .findById(taskId)
        .orElseThrow(() -> new IllegalArgumentException("Processing task not found: " + taskId));
  }

  private void recordTaskDuration(ProcessingTask task) {
    if (task.getLockedAt() == null) {
      return;
    }

    metricsService.recordTaskDuration(
        task.getTaskType(), Duration.between(task.getLockedAt(), Instant.now(clock)));
  }
}
