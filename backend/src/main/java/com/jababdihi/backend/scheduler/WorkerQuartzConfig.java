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
  JobDetail hourlyBackfillJobDetail() {
    return JobBuilder.newJob(HourlyBackfillJob.class)
        .withIdentity("hourlyBackfillJob", "ingestion")
        .storeDurably()
        .build();
  }

  @Bean
  Trigger hourlyBackfillTrigger(JobDetail hourlyBackfillJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(hourlyBackfillJobDetail)
        .withIdentity("hourlyBackfillTrigger", "ingestion")
        .withSchedule(CronScheduleBuilder.cronSchedule("0 0 * * * ?"))
        .build();
  }

  @Bean
  JobDetail dailyRollingIngestionJobDetail() {
    return JobBuilder.newJob(DailyRollingIngestionJob.class)
        .withIdentity("dailyRollingIngestionJob", "ingestion")
        .storeDurably()
        .build();
  }

  @Bean
  Trigger dailyRollingIngestionTrigger(JobDetail dailyRollingIngestionJobDetail) {
    return TriggerBuilder.newTrigger()
        .forJob(dailyRollingIngestionJobDetail)
        .withIdentity("dailyRollingIngestionTrigger", "ingestion")
        .withSchedule(
            CronScheduleBuilder.cronSchedule("0 0 2 * * ?")
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
