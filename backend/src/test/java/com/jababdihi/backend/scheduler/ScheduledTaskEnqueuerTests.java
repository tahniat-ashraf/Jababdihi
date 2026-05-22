package com.jababdihi.backend.scheduler;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jababdihi.backend.taskqueue.ProcessingTaskService;
import com.jababdihi.backend.taskqueue.ProcessingTaskType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScheduledTaskEnqueuerTests {
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-03-04T20:30:00Z"), ZoneOffset.UTC);

  private final ProcessingTaskService taskService = mock(ProcessingTaskService.class);
  private final ScheduledTaskEnqueuer enqueuer = new ScheduledTaskEnqueuer(taskService, CLOCK);

  @Test
  void enqueueHourlyBackfillPlaceholderTargetsAccountabilityStartDate() {
    enqueuer.enqueueHourlyBackfillPlaceholder();

    verify(taskService)
        .enqueue(
            eq(ProcessingTaskType.CRAWL_SOURCE_DAY), contains("\"targetDate\":\"2026-02-17\""));
  }

  @Test
  void enqueueDailyRollingWindowPlaceholdersTargetsThreeDhakaDates() {
    enqueuer.enqueueDailyRollingWindowPlaceholders();

    verify(taskService)
        .enqueue(
            eq(ProcessingTaskType.CRAWL_SOURCE_DAY), contains("\"targetDate\":\"2026-03-05\""));
    verify(taskService)
        .enqueue(
            eq(ProcessingTaskType.CRAWL_SOURCE_DAY), contains("\"targetDate\":\"2026-03-04\""));
    verify(taskService)
        .enqueue(
            eq(ProcessingTaskType.CRAWL_SOURCE_DAY), contains("\"targetDate\":\"2026-03-03\""));
  }

  @Test
  void enqueueAiRetryPlaceholdersRequeuesDueTasksAndAddsAiPipelineTasks() {
    when(taskService.requeueDueRetryableTasks()).thenReturn(2);

    enqueuer.enqueueAiRetryPlaceholders();

    verify(taskService)
        .enqueueAll(
            eq(
                List.of(
                    ProcessingTaskType.EXTRACT_INCIDENT,
                    ProcessingTaskType.GENERATE_EMBEDDING,
                    ProcessingTaskType.CORRELATE_INCIDENT,
                    ProcessingTaskType.GENERATE_TRANSLATIONS,
                    ProcessingTaskType.RECOMPUTE_CONFIDENCE)),
            contains("\"requeuedTasks\":2"));
  }
}
