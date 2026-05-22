package com.jababdihi.backend.ingestion;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RssFeedParserTests {
  private final RssFeedParser parser = new RssFeedParser(new ExtractedTextCleaner());

  @Test
  void parseRssFeedExtractsLinkTitleDateAndCleanText() {
    String xml =
        """
        <rss version="2.0" xmlns:content="http://purl.org/rss/1.0/modules/content/">
          <channel>
            <item>
              <title>Sample title</title>
              <link>https://www.thedailystar.net/news/story?utm_source=rss</link>
              <pubDate>Fri, 22 May 2026 10:15:00 GMT</pubDate>
              <content:encoded><![CDATA[<p>Article <strong>body</strong></p>]]></content:encoded>
            </item>
          </channel>
        </rss>
        """;

    List<FeedItem> items = parser.parse(xml);

    assertThat(items).hasSize(1);
    assertThat(items.getFirst().sourceUrl())
        .isEqualTo("https://www.thedailystar.net/news/story?utm_source=rss");
    assertThat(items.getFirst().title()).isEqualTo("Sample title");
    assertThat(items.getFirst().publishedAt()).contains(Instant.parse("2026-05-22T10:15:00Z"));
    assertThat(items.getFirst().extractedText()).isEqualTo("Article body");
  }

  @Test
  void parseAtomFeedExtractsAlternateLink() {
    String xml =
        """
        <feed xmlns="http://www.w3.org/2005/Atom">
          <entry>
            <title>Atom title</title>
            <link rel="alternate" href="https://www.dhakatribune.com/bangladesh/1" />
            <updated>2026-05-22T10:15:00Z</updated>
            <summary>Short summary</summary>
          </entry>
        </feed>
        """;

    List<FeedItem> items = parser.parse(xml);

    assertThat(items).hasSize(1);
    assertThat(items.getFirst().sourceUrl()).isEqualTo("https://www.dhakatribune.com/bangladesh/1");
    assertThat(items.getFirst().extractedText()).isEqualTo("Short summary");
  }
}
