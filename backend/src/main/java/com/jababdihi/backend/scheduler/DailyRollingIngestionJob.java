package com.jababdihi.backend.scheduler;

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
public class DailyRollingIngestionJob implements Job {
  @Autowired private ScheduledTaskEnqueuer scheduledTaskEnqueuer;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    scheduledTaskEnqueuer.enqueueDailyRollingWindowPlaceholders();
  }
}
