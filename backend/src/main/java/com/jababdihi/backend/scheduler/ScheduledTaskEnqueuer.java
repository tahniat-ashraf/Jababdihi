package com.jababdihi.backend.scheduler;

import com.jababdihi.backend.taskqueue.ProcessingTaskService;
import com.jababdihi.backend.taskqueue.ProcessingTaskType;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
public class ScheduledTaskEnqueuer {
  static final LocalDate BACKFILL_START_DATE = LocalDate.of(2026, 2, 17);
  static final ZoneId DHAKA_ZONE = ZoneId.of("Asia/Dhaka");

  private static final List<ProcessingTaskType> AI_PIPELINE_TASKS =
      List.of(
          ProcessingTaskType.EXTRACT_INCIDENT,
          ProcessingTaskType.GENERATE_EMBEDDING,
          ProcessingTaskType.CORRELATE_INCIDENT,
          ProcessingTaskType.GENERATE_TRANSLATIONS,
          ProcessingTaskType.RECOMPUTE_CONFIDENCE);

  private final ProcessingTaskService taskService;
  private final Clock clock;

  ScheduledTaskEnqueuer(ProcessingTaskService taskService, Clock clock) {
    this.taskService = taskService;
    this.clock = clock;
  }

  public void enqueueHourlyBackfillPlaceholder() {
    taskService.enqueue(
        ProcessingTaskType.CRAWL_SOURCE_DAY,
        """
        {"job":"hourly_backfill","targetDate":"%s"}
        """
            .formatted(BACKFILL_START_DATE));
  }

  public void enqueueDailyRollingWindowPlaceholders() {
    LocalDate today = LocalDate.now(clock.withZone(DHAKA_ZONE));
    for (int offset = 0; offset < 3; offset++) {
      LocalDate targetDate = today.minusDays(offset);
      taskService.enqueue(
          ProcessingTaskType.CRAWL_SOURCE_DAY,
          """
          {"job":"daily_rolling_ingestion","targetDate":"%s"}
          """
              .formatted(targetDate));
    }
  }

  public int enqueueAiRetryPlaceholders() {
    int requeuedTasks = taskService.requeueDueRetryableTasks();
    taskService.enqueueAll(
        AI_PIPELINE_TASKS,
        """
        {"job":"ai_task_retry","requeuedTasks":%d}
        """
            .formatted(requeuedTasks));
    return requeuedTasks;
  }
}
