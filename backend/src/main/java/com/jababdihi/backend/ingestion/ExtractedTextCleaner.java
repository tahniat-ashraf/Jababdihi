package com.jababdihi.backend.ingestion;

import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

@Component
class ExtractedTextCleaner {
  String clean(String value) {
    if (value == null || value.isBlank()) {
      return "";
    }

    String withoutScripts =
        value
            .replaceAll("(?is)<script[^>]*>.*?</script>", " ")
            .replaceAll("(?is)<style[^>]*>.*?</style>", " ");
    String withoutTags = withoutScripts.replaceAll("(?s)<[^>]+>", " ");
    return HtmlUtils.htmlUnescape(withoutTags).trim().replaceAll("\\s+", " ");
  }
}
