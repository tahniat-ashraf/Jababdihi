package com.jababdihi.backend.ingestion;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Component
class RssFeedParser {
  private final ExtractedTextCleaner textCleaner;

  RssFeedParser(ExtractedTextCleaner textCleaner) {
    this.textCleaner = textCleaner;
  }

  List<FeedItem> parse(String xml) {
    if (xml == null || xml.isBlank()) {
      return List.of();
    }

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setExpandEntityReferences(false);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      Document document =
          factory
              .newDocumentBuilder()
              .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
      document.getDocumentElement().normalize();

      NodeList rssItems = document.getElementsByTagName("item");
      if (rssItems.getLength() > 0) {
        return parseRssItems(rssItems);
      }

      return parseAtomEntries(document.getElementsByTagName("entry"));
    } catch (Exception ex) {
      throw new IllegalArgumentException("Unable to parse publisher RSS feed", ex);
    }
  }

  private List<FeedItem> parseRssItems(NodeList nodes) {
    List<FeedItem> items = new ArrayList<>();
    for (int index = 0; index < nodes.getLength(); index++) {
      Element item = (Element) nodes.item(index);
      String sourceUrl = childText(item, "link");
      String title = childText(item, "title");
      Optional<Instant> publishedAt =
          parseDate(firstPresent(childText(item, "pubDate"), childText(item, "date")));
      String text =
          textCleaner.clean(
              firstPresent(
                  namespacedChildText(item, "encoded"),
                  childText(item, "description"),
                  childText(item, "summary")));
      if (!sourceUrl.isBlank()) {
        items.add(new FeedItem(sourceUrl, title, publishedAt, text));
      }
    }
    return items;
  }

  private List<FeedItem> parseAtomEntries(NodeList nodes) {
    List<FeedItem> items = new ArrayList<>();
    for (int index = 0; index < nodes.getLength(); index++) {
      Element entry = (Element) nodes.item(index);
      String sourceUrl = atomLink(entry);
      String title = childText(entry, "title");
      Optional<Instant> publishedAt =
          parseDate(firstPresent(childText(entry, "published"), childText(entry, "updated")));
      String text =
          textCleaner.clean(firstPresent(childText(entry, "content"), childText(entry, "summary")));
      if (!sourceUrl.isBlank()) {
        items.add(new FeedItem(sourceUrl, title, publishedAt, text));
      }
    }
    return items;
  }

  private String atomLink(Element entry) {
    NodeList links = entry.getElementsByTagName("link");
    for (int index = 0; index < links.getLength(); index++) {
      Element link = (Element) links.item(index);
      String rel = link.getAttribute("rel");
      if (rel.isBlank() || rel.equals("alternate")) {
        String href = link.getAttribute("href");
        if (!href.isBlank()) {
          return href;
        }
      }
    }
    return childText(entry, "link");
  }

  private String childText(Element parent, String tagName) {
    NodeList nodes = parent.getElementsByTagName(tagName);
    if (nodes.getLength() == 0) {
      return "";
    }
    Node node = nodes.item(0);
    return node.getTextContent() == null ? "" : node.getTextContent().trim();
  }

  private String namespacedChildText(Element parent, String localName) {
    NodeList nodes = parent.getElementsByTagNameNS("*", localName);
    if (nodes.getLength() == 0) {
      return "";
    }
    Node node = nodes.item(0);
    return node.getTextContent() == null ? "" : node.getTextContent().trim();
  }

  private String firstPresent(String... values) {
    for (String value : values) {
      if (value != null && !value.isBlank()) {
        return value;
      }
    }
    return "";
  }

  private Optional<Instant> parseDate(String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }

    List<DateParser> parsers =
        List.of(
            date -> ZonedDateTime.parse(date, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant(),
            date -> Instant.parse(date),
            date -> OffsetDateTime.parse(date, DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant(),
            date -> ZonedDateTime.parse(date, DateTimeFormatter.ISO_ZONED_DATE_TIME).toInstant());
    for (DateParser parser : parsers) {
      try {
        return Optional.of(parser.parse(value.trim()));
      } catch (DateTimeParseException ignored) {
        // Try the next common RSS/Atom date representation.
      }
    }
    return Optional.empty();
  }

  @FunctionalInterface
  private interface DateParser {
    Instant parse(String value);
  }
}
