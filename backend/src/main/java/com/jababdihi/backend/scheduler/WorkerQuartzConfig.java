package com.jababdihi.backend.scheduler;

import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("worker")
public class WorkerQuartzConfig {

  @Bean
  JobDetail currentIngestionJobDetail() {
    return JobBuilder.newJob(CurrentIngestionJob.class)
        .withIdentity("currentIngestionJob", "ingestion")
        .storeDurably()
        .build();
  }

  @Bean
  Trigger currentIngestionTrigger(JobDetail currentIngestionJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(currentIngestionJobDetail)
        .withIdentity("currentIngestionTrigger", "ingestion")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
        .build();
  }

  @Bean
  JobDetail backfillJobDetail() {
    return JobBuilder.newJob(HourlyBackfillJob.class)
        .withIdentity("backfillJob", "ingestion")
        .storeDurably()
        .build();
  }

  @Bean
  Trigger backfillTrigger(JobDetail backfillJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(backfillJobDetail)
        .withIdentity("backfillTrigger", "ingestion")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 0 */2 * * ?")
                .inTimeZone(TimeZone.getTimeZone(ScheduledTaskEnqueuer.DHAKA_ZONE)))
        .build();
  }

  @Bean
  JobDetail aiTaskRetryJobDetail() {
    return JobBuilder.newJob(AiTaskRetryJob.class)
        .withIdentity("aiTaskRetryJob", "taskqueue")
        .storeDurably()
        .build();
  }

  @Bean
  Trigger aiTaskRetryTrigger(JobDetail aiTaskRetryJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(aiTaskRetryJobDetail)
        .withIdentity("aiTaskRetryTrigger", "taskqueue")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?"))
        .build();
  }
}
