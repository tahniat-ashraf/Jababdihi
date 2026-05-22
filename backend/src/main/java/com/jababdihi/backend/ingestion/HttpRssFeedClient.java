package com.jababdihi.backend.ingestion;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("worker")
class HttpRssFeedClient implements FeedClient {
  private static final String USER_AGENT = "JababdihiBot/0.1 (+https://github.com/tahniat-ashraf)";

  private final HttpClient httpClient;
  private final RssFeedParser parser;

  HttpRssFeedClient(HttpClient httpClient, RssFeedParser parser) {
    this.httpClient = httpClient;
    this.parser = parser;
  }

  @Override
  public List<FeedItem> fetch(IngestionProperties.PublisherFeedProperties publisher) {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(publisher.getFeedUrl()))
            .timeout(Duration.ofSeconds(20))
            .header("User-Agent", USER_AGENT)
            .header(
                "Accept", "application/rss+xml, application/atom+xml, application/xml, text/xml")
            .GET()
            .build();
    try {
      HttpResponse<String> response =
          httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        return List.of();
      }
      return parser.parse(response.body());
    } catch (IOException ex) {
      throw new IllegalStateException("Unable to fetch RSS feed for " + publisher.getName(), ex);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(
          "RSS feed fetch was interrupted for " + publisher.getName(), ex);
    }
  }
}
