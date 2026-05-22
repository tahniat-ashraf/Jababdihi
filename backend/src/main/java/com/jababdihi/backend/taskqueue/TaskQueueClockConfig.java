package com.jababdihi.backend.taskqueue;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class TaskQueueClockConfig {

  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }
}
