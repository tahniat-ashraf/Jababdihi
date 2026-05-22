package com.jababdihi.backend.incident.processing;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("worker")
@EnableConfigurationProperties(IncidentProcessingProperties.class)
class WorkerIncidentProcessingConfig {}
