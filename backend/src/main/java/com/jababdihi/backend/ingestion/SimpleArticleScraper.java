package com.jababdihi.backend.ingestion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class SimpleArticleScraper implements DirectArticleScraper {
  private static final Pattern TITLE_PATTERN =
      Pattern.compile("(?is)<title[^>]*>(.*?)</title>", Pattern.CASE_INSENSITIVE);
  private static final String USER_AGENT = "JababdihiBot/0.1 (+https://github.com/tahniat-ashraf)";

  private final HttpClient httpClient;
  private final ExtractedTextCleaner textCleaner;

  SimpleArticleScraper(HttpClient httpClient, ExtractedTextCleaner textCleaner) {
    this.httpClient = httpClient;
    this.textCleaner = textCleaner;
  }

  @Override
  public Optional<ScrapedArticle> scrape(
      String canonicalUrl, IngestionProperties.PublisherFeedProperties publisher) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(canonicalUrl))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml")
            .GET()
            .build();

    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        return Optional.empty();
      }

      String html = response.body();
      String title = extractTitle(html);
      String text = textCleaner.clean(html);
      if (text.isBlank()) {
        return Optional.empty();
      }
      return Optional.of(new ScrapedArticle(title, Optional.empty(), text));
    } catch (IllegalArgumentException | IOException ex) {
      return Optional.empty();
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
  }

  private String extractTitle(String html) {
    Matcher matcher = TITLE_PATTERN.matcher(html);
    if (!matcher.find()) {
      return "";
    }
    return textCleaner.clean(matcher.group(1));
  }
}
