package com.jababdihi.backend.observability;

import com.jababdihi.backend.taskqueue.ProcessingTaskRepository;
import com.jababdihi.backend.taskqueue.ProcessingTaskStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

@Component
class QueueMetricsBinder implements MeterBinder {
  private final ProcessingTaskRepository taskRepository;

  QueueMetricsBinder(ProcessingTaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  @Override
  public void bindTo(MeterRegistry registry) {
    for (ProcessingTaskStatus status : ProcessingTaskStatus.values()) {
      Gauge.builder("jababdihi_queue_depth", () -> taskRepository.countByStatus(status))
          .description("Processing task queue depth by status")
          .tag("status", status.name())
          .register(registry);
    }
  }
}
