package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.ingestion.RawContent;

interface IncidentExtractor {
  IncidentCandidate extract(RawContent rawContent);
}
