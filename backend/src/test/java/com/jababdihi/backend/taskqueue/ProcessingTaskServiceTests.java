package com.jababdihi.backend.taskqueue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

class ProcessingTaskServiceTests {
  private static final Instant NOW = Instant.parse("2026-03-01T00:00:00Z");

  private final ProcessingTaskRepository repository = mock(ProcessingTaskRepository.class);
  private final ProcessingTaskService service =
      new ProcessingTaskService(
          repository, new TaskBackoffPolicy(), Clock.fixed(NOW, ZoneOffset.UTC));

  @Test
  void enqueueCreatesPendingTaskWithDefaultAttempts() {
    when(repository.save(any(ProcessingTask.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ProcessingTask task =
        service.enqueue(ProcessingTaskType.CRAWL_SOURCE_DAY, "{\"targetDate\":\"2026-03-01\"}");

    assertThat(task.getTaskType()).isEqualTo(ProcessingTaskType.CRAWL_SOURCE_DAY);
    assertThat(task.getStatus()).isEqualTo(ProcessingTaskStatus.PENDING);
    assertThat(task.getMaxAttempts()).isEqualTo(5);
    assertThat(task.getAvailableAt()).isEqualTo(NOW);
  }

  @Test
  void claimNextAvailableMarksPendingTaskRunningAndIncrementsAttempt() {
    ProcessingTask task = new ProcessingTask(ProcessingTaskType.EXTRACT_INCIDENT, "{}", NOW);
    when(repository.findDueTasksForUpdate(
            eq(ProcessingTaskStatus.PENDING), eq(NOW), any(Pageable.class)))
        .thenReturn(List.of(task));

    Optional<ProcessingTask> claimedTask = service.claimNextAvailable("worker-1");

    assertThat(claimedTask).contains(task);
    assertThat(task.getStatus()).isEqualTo(ProcessingTaskStatus.RUNNING);
    assertThat(task.getAttemptCount()).isEqualTo(1);
    assertThat(task.getLockedAt()).isEqualTo(NOW);
    assertThat(task.getLockedBy()).isEqualTo("worker-1");
  }

  @Test
  void markFailedSchedulesRetryableTaskWithBackoff() {
    UUID taskId = UUID.randomUUID();
    ProcessingTask task = new ProcessingTask(ProcessingTaskType.EXTRACT_INCIDENT, "{}", NOW);
    task.setAttemptCount(2);
    when(repository.findById(taskId)).thenReturn(Optional.of(task));

    ProcessingTask failedTask = service.markFailed(taskId, "temporary failure");

    assertThat(failedTask.getStatus()).isEqualTo(ProcessingTaskStatus.FAILED_RETRYABLE);
    assertThat(failedTask.getAvailableAt()).isEqualTo(NOW.plusSeconds(600));
    assertThat(failedTask.getLastError()).isEqualTo("temporary failure");
  }

  @Test
  void markFailedDeadLettersAtMaxAttempts() {
    UUID taskId = UUID.randomUUID();
    ProcessingTask task = new ProcessingTask(ProcessingTaskType.EXTRACT_INCIDENT, "{}", NOW);
    task.setAttemptCount(5);
    when(repository.findById(taskId)).thenReturn(Optional.of(task));

    ProcessingTask failedTask = service.markFailed(taskId, "permanent failure");

    assertThat(failedTask.getStatus()).isEqualTo(ProcessingTaskStatus.FAILED_PERMANENTLY);
  }

  @Test
  void requeueDueRetryableTasksMarksDueTasksPending() {
    ProcessingTask task = new ProcessingTask(ProcessingTaskType.EXTRACT_INCIDENT, "{}", NOW);
    task.setStatus(ProcessingTaskStatus.FAILED_RETRYABLE);
    when(repository.findDueTasksForUpdate(
            eq(ProcessingTaskStatus.FAILED_RETRYABLE), eq(NOW), any(Pageable.class)))
        .thenReturn(List.of(task));

    int requeuedCount = service.requeueDueRetryableTasks();

    assertThat(requeuedCount).isEqualTo(1);
    assertThat(task.getStatus()).isEqualTo(ProcessingTaskStatus.PENDING);
  }

  @Test
  void enqueueAllCreatesOneTaskPerType() {
    when(repository.save(any(ProcessingTask.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    service.enqueueAll(
        List.of(ProcessingTaskType.EXTRACT_INCIDENT, ProcessingTaskType.GENERATE_EMBEDDING), "{}");

    ArgumentCaptor<ProcessingTask> taskCaptor = ArgumentCaptor.forClass(ProcessingTask.class);
    verify(repository, org.mockito.Mockito.times(2)).save(taskCaptor.capture());

    assertThat(taskCaptor.getAllValues())
        .extracting(ProcessingTask::getTaskType)
        .containsExactly(
            ProcessingTaskType.EXTRACT_INCIDENT, ProcessingTaskType.GENERATE_EMBEDDING);
  }
}
