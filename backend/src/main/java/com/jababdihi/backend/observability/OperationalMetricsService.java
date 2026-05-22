package com.jababdihi.backend.observability;

import com.jababdihi.backend.taskqueue.ProcessingTaskType;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class OperationalMetricsService {
  private final MeterRegistry meterRegistry;

  OperationalMetricsService(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  public void recordJobSucceeded(String jobName) {
    meterRegistry
        .counter("jababdihi_job_runs_total", "job", jobName, "status", "succeeded")
        .increment();
  }

  public void recordJobFailed(String jobName) {
    meterRegistry
        .counter("jababdihi_job_runs_total", "job", jobName, "status", "failed")
        .increment();
  }

  public void recordCrawlerFetch(String publisher, String status) {
    meterRegistry
        .counter("jababdihi_crawler_fetches_total", "publisher", publisher, "status", status)
        .increment();
  }

  public void recordTaskDuration(ProcessingTaskType taskType, Duration duration) {
    meterRegistry.timer("jababdihi_task_duration", "task_type", taskType.name()).record(duration);
  }
}
