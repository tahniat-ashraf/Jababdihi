package com.jababdihi.backend.scheduler;

import com.jababdihi.backend.observability.OperationalMetricsService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("worker")
@DisallowConcurrentExecution
public class AiTaskRetryJob implements Job {
  private static final String JOB_NAME = "ai_task_retry";

  @Autowired private ScheduledTaskEnqueuer scheduledTaskEnqueuer;
  @Autowired private OperationalMetricsService metricsService;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    try {
      scheduledTaskEnqueuer.enqueueAiRetryPlaceholders();
      metricsService.recordJobSucceeded(JOB_NAME);
    } catch (RuntimeException ex) {
      metricsService.recordJobFailed(JOB_NAME);
      throw new JobExecutionException(ex);
    }
  }
}
