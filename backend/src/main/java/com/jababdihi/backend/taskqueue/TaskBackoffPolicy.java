package com.jababdihi.backend.taskqueue;

import java.time.Duration;
import org.springframework.stereotype.Component;

@Component
public class TaskBackoffPolicy {
  public static final int DEFAULT_MAX_ATTEMPTS = 5;
  private static final Duration INITIAL_DELAY = Duration.ofMinutes(5);
  private static final Duration MAX_DELAY = Duration.ofHours(6);

  Duration nextDelay(int attemptCount) {
    int retryNumber = Math.max(1, attemptCount);
    long multiplier = 1L << Math.min(retryNumber - 1, 20);
    Duration delay = INITIAL_DELAY.multipliedBy(multiplier);

    return delay.compareTo(MAX_DELAY) > 0 ? MAX_DELAY : delay;
  }
}
