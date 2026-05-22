package com.jababdihi.backend.taskqueue;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class TaskBackoffPolicyTests {
  private final TaskBackoffPolicy policy = new TaskBackoffPolicy();

  @Test
  void nextDelayUsesExponentialBackoffWithSixHourCap() {
    assertThat(policy.nextDelay(1)).isEqualTo(Duration.ofMinutes(5));
    assertThat(policy.nextDelay(2)).isEqualTo(Duration.ofMinutes(10));
    assertThat(policy.nextDelay(3)).isEqualTo(Duration.ofMinutes(20));
    assertThat(policy.nextDelay(8)).isEqualTo(Duration.ofHours(6));
    assertThat(policy.nextDelay(20)).isEqualTo(Duration.ofHours(6));
  }
}
