package com.jababdihi.backend.ingestion;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
class CanonicalUrlNormalizer {
  private static final List<String> EXACT_TRACKING_PARAMS =
      List.of(
          "fbclid", "gclid", "dclid", "gbraid", "wbraid", "mc_cid", "mc_eid", "yclid", "igshid",
          "ref", "source", "spm");

  Optional<String> normalize(String sourceUrl, String allowedDomain) {
    if (sourceUrl == null || sourceUrl.isBlank()) {
      return Optional.empty();
    }

    try {
      URI sourceUri = new URI(sourceUrl.trim()).normalize();
      String scheme = normalizedScheme(sourceUri);
      String host = normalizedHost(sourceUri);
      String domain = allowedDomain.toLowerCase(Locale.ROOT);
      if (scheme == null || host == null || !isAllowedHost(host, domain)) {
        return Optional.empty();
      }

      String path = normalizedPath(sourceUri);
      String query = normalizedQuery(sourceUri.getRawQuery());
      URI canonicalUri = new URI(scheme, null, host, sourceUri.getPort(), path, query, null);
      return Optional.of(canonicalUri.toASCIIString());
    } catch (IllegalArgumentException | URISyntaxException ex) {
      return Optional.empty();
    }
  }

  private String normalizedScheme(URI uri) {
    if (uri.getScheme() == null) {
      return null;
    }

    String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
    if (!scheme.equals("http") && !scheme.equals("https")) {
      return null;
    }
    return scheme;
  }

  private String normalizedHost(URI uri) {
    if (uri.getHost() == null) {
      return null;
    }
    return uri.getHost().toLowerCase(Locale.ROOT);
  }

  private boolean isAllowedHost(String host, String domain) {
    return host.equals(domain) || host.endsWith("." + domain);
  }

  private String normalizedPath(URI uri) {
    String path = uri.getPath();
    if (path == null || path.isBlank()) {
      return "/";
    }
    if (path.length() > 1 && path.endsWith("/")) {
      return path.substring(0, path.length() - 1);
    }
    return path;
  }

  private String normalizedQuery(String rawQuery) {
    if (rawQuery == null || rawQuery.isBlank()) {
      return null;
    }

    List<QueryParam> params = new ArrayList<>();
    for (String segment : rawQuery.split("&")) {
      if (segment.isBlank()) {
        continue;
      }

      String[] parts = segment.split("=", 2);
      String name = decode(parts[0]);
      if (isTrackingParam(name)) {
        continue;
      }
      String value = parts.length > 1 ? decode(parts[1]) : "";
      params.add(new QueryParam(name, value));
    }

    if (params.isEmpty()) {
      return null;
    }

    params.sort(QueryParam::compareTo);
    return String.join("&", params.stream().map(QueryParam::encoded).toList());
  }

  private boolean isTrackingParam(String name) {
    String lowerName = name.toLowerCase(Locale.ROOT);
    return lowerName.startsWith("utm_") || EXACT_TRACKING_PARAMS.contains(lowerName);
  }

  private String decode(String value) {
    return URLDecoder.decode(value, StandardCharsets.UTF_8);
  }

  private record QueryParam(String name, String value) implements Comparable<QueryParam> {
    @Override
    public int compareTo(QueryParam other) {
      int nameComparison = name.compareTo(other.name);
      if (nameComparison != 0) {
        return nameComparison;
      }
      return value.compareTo(other.value);
    }

    String encoded() {
      return encodeValue(name) + "=" + encodeValue(value);
    }

    private String encodeValue(String value) {
      return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
    }
  }
}
