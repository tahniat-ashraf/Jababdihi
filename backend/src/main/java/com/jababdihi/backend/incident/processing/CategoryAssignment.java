package com.jababdihi.backend.incident.processing;

import com.jababdihi.backend.common.CategoryCode;

record CategoryAssignment(CategoryCode categoryCode, boolean matchedKeywordRule) {}
