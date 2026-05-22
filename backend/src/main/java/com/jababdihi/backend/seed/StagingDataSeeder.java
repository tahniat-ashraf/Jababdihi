package com.jababdihi.backend.seed;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Inserts deterministic staging data for smoke-testing public localization and review flows.
 *
 * <p>Activated only when {@code --app.seed=staging} is passed on the command line.
 *
 * <pre>
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=api,seed \
 *       -Dspring-boot.run.arguments="--app.seed=staging"
 * </pre>
 */
/**
 * Safety guard: never activate on the production profile.
 *
 * <p>{@code @Profile("!prod")} ensures the bean is not created when the "prod" Spring profile is
 * active, providing defence-in-depth on top of the {@code @ConditionalOnProperty} guard. Even if
 * {@code --app.seed=staging} were accidentally passed to a production container, this annotation
 * prevents the seeder from running.
 */
@Component
@Profile("!prod")
@ConditionalOnProperty(name = "app.seed", havingValue = "staging")
class StagingDataSeeder implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(StagingDataSeeder.class);
  private static final String SOURCE_PREFIX = "https://seed.jababdihi.local/";

  private final JdbcTemplate jdbc;

  StagingDataSeeder(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(ApplicationArguments args) {
    Integer existing =
        jdbc.queryForObject(
            "select count(*) from incident_sources where source_url like ?",
            Integer.class,
            SOURCE_PREFIX + "%");
    if (existing != null && existing > 0) {
      log.info("Staging seed data already present - skipping");
      System.exit(0);
      return;
    }

    Map<String, UUID> publishers =
        Map.of(
            "Prothom Alo",
            publisher("Prothom Alo", "prothomalo.com", "https://www.prothomalo.com"),
            "The Daily Star",
            publisher("The Daily Star", "thedailystar.net", "https://www.thedailystar.net"),
            "bdnews24",
            publisher("bdnews24", "bdnews24.com", "https://bdnews24.com"),
            "Dhaka Tribune",
            publisher("Dhaka Tribune", "dhakatribune.com", "https://www.dhakatribune.com"),
            "New Age",
            publisher("New Age", "newagebd.net", "https://www.newagebd.net"));

    UUID dhaka = location("Dhaka", "Dhaka", "Mirpur");
    UUID chattogram = location("Chattogram", "Chattogram", null);

    log.info("Seeding localized staging incidents...");
    for (SeedIncident incident : seedIncidents(dhaka, chattogram)) {
      insertIncident(incident, publishers);
    }
    log.info(
        "Staging seed complete - {} representative incidents created",
        seedIncidents(dhaka, chattogram).size());
    System.exit(0);
  }

  private List<SeedIncident> seedIncidents(UUID dhaka, UUID chattogram) {
    return List.of(
        incident(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            88,
            "HIGH",
            dhaka,
            "Mirpur, Dhaka",
            LocalDate.of(2026, 3, 18),
            "সরকার-সংশ্লিষ্ট নেতার বিরুদ্ধে চাঁদাবাজির অভিযোগ",
            "একাধিক উৎসের প্রতিবেদনে ঢাকায় সরকার-সংশ্লিষ্ট একজন স্থানীয় নেতার বিরুদ্ধে"
                + " ব্যবসায়ীদের কাছ থেকে অর্থ দাবির অভিযোগ উঠে এসেছে।",
            "Government-linked leader accused of extortion",
            "Multiple sources reported allegations that a government-linked local leader in"
                + " Dhaka demanded payments from traders.",
            List.of("EXTORTION", "ABUSE_OF_POWER"),
            List.of("Prothom Alo", "The Daily Star", "bdnews24", "Dhaka Tribune", "New Age")),
        incident(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            58,
            "MODERATE",
            dhaka,
            "Dhaka",
            LocalDate.of(2026, 3, 17),
            "সরকারি কর্মকর্তার বিরুদ্ধে ক্ষমতার অপব্যবহারের অভিযোগ",
            "সূত্র অনুযায়ী, একটি প্রশাসনিক অনুমোদন ঘিরে ক্ষমতার অপব্যবহারের অভিযোগ রয়েছে।",
            "Official accused of administrative abuse of power",
            "Sources reported allegations of administrative abuse of power around a permit"
                + " approval.",
            List.of("ABUSE_OF_POWER"),
            List.of("Prothom Alo", "bdnews24")),
        incident(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            84,
            "HIGH",
            chattogram,
            "Chattogram",
            LocalDate.of(2026, 3, 16),
            "বিরোধী দলের স্থানীয় নেতার বিরুদ্ধে সশস্ত্র হামলার অভিযোগ",
            "একাধিক প্রতিবেদনে বিরোধী দলের স্থানীয় এক নেতার বিরুদ্ধে সশস্ত্র হামলায়"
                + " জড়িত থাকার অভিযোগ প্রকাশিত হয়েছে।",
            "Opposition local leader accused in armed attack",
            "Multiple reports alleged that a local Opposition leader was involved in an armed"
                + " attack.",
            List.of("ARMED_THREAT_ATTACK", "POLITICAL_VIOLENCE"),
            List.of("The Daily Star", "Dhaka Tribune", "New Age", "bdnews24")),
        incident(
            "GOVERNMENT",
            "MANUALLY_PUBLISHED",
            76,
            "HIGH",
            dhaka,
            "Dhaka",
            LocalDate.of(2026, 3, 15),
            "বাংলা-শুধু অনুবাদসহ জমি দখলের অভিযোগ",
            "এই ঘটনাটির ইংরেজি অনুবাদ এখনো প্রস্তুত নয়, তাই ইংরেজি অনুরোধে বাংলা পাঠ"
                + " দেখানো হবে।",
            null,
            null,
            List.of("LAND_GRABBING"),
            List.of("Prothom Alo", "bdnews24", "Dhaka Tribune")),
        incident(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            79,
            "HIGH",
            null,
            null,
            LocalDate.of(2026, 3, 14),
            "জেলা অনির্দিষ্ট রেখে বিরোধী সংশ্লিষ্ট দুর্নীতির অভিযোগ",
            "প্রতিবেদনগুলোতে জেলা স্পষ্ট নয়, তবে বিরোধী-সংশ্লিষ্ট ব্যক্তির বিরুদ্ধে"
                + " দুর্নীতির অভিযোগ রয়েছে।",
            "Opposition-linked corruption allegation with missing district",
            "Reports did not identify a district, but described corruption allegations involving"
                + " an Opposition-linked figure.",
            List.of("CORRUPTION_BRIBERY"),
            List.of("The Daily Star", "New Age", "bdnews24")),
        incident(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            82,
            "HIGH",
            dhaka,
            "Dhaka",
            LocalDate.of(2026, 3, 13),
            "অস্বাভাবিক দীর্ঘ শিরোনামসহ শিক্ষা প্রতিষ্ঠানে হামলা, ভয়ভীতি প্রদর্শন ও"
                + " রাজনৈতিক প্রভাব খাটানোর অভিযোগ যা কার্ডের লেখার সীমা পরীক্ষা করে",
            "একাধিক উৎসের প্রতিবেদনে একটি শিক্ষা প্রতিষ্ঠানে হামলা ও ভয়ভীতি প্রদর্শনের"
                + " অভিযোগ রয়েছে।",
            "Very long title alleging attack on an educational institution, intimidation, and"
                + " political pressure to test card wrapping behavior",
            "Multiple sources reported allegations of an attack and intimidation at an"
                + " educational institution.",
            List.of("ATTACK_ON_INSTITUTION", "INTIMIDATION", "POLITICAL_VIOLENCE"),
            List.of("Prothom Alo", "The Daily Star", "bdnews24", "Dhaka Tribune", "New Age")),
        incident(
            "OPPOSITION",
            "MANUALLY_PUBLISHED",
            63,
            "MODERATE",
            chattogram,
            "Chattogram",
            LocalDate.of(2026, 3, 12),
            "বিরোধী দলের বিরুদ্ধে নির্বাচনী বাধার অভিযোগ",
            "সূত্র অনুযায়ী, ভোটকেন্দ্রে বাধা ও ভয়ভীতি প্রদর্শনের অভিযোগ রয়েছে।",
            "Opposition accused of election obstruction",
            "Sources reported allegations of obstruction and intimidation around polling.",
            List.of("ELECTION_VIOLENCE", "INTIMIDATION"),
            List.of("Dhaka Tribune", "New Age")),
        incident(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            71,
            "HIGH",
            dhaka,
            "Dhaka",
            LocalDate.of(2026, 3, 11),
            "ইংরেজি প্রতিবেদনের জন্য দ্বিভাষিক সহিংসতার অভিযোগ",
            "বাংলা সারসংক্ষেপসহ ইংরেজি উৎসভিত্তিক একটি অভিযোগ।",
            "English-source political violence allegation",
            "An English-source incident with a complete Bangla summary for bilingual feed"
                + " testing.",
            List.of("POLITICAL_VIOLENCE"),
            List.of("The Daily Star", "Dhaka Tribune", "New Age")),
        incident(
            "GOVERNMENT",
            "PENDING_REVIEW",
            42,
            "LOW",
            dhaka,
            "Dhaka",
            LocalDate.of(2026, 3, 10),
            "পর্যালোচনাধীন অভিযোগ",
            "এই ঘটনাটি মানব-পর্যালোচনার অপেক্ষায় থাকবে এবং জনসম্মুখে প্রকাশিত হবে না।",
            "Pending review incident",
            "This incident should remain internal and must not appear in the public feed.",
            List.of("EXTORTION"),
            List.of("Prothom Alo", "bdnews24")));
  }

  private void insertIncident(SeedIncident row, Map<String, UUID> publishers) {
    UUID incidentId = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());
    int sourceCount = row.publisherNames().size();

    jdbc.update(
        "insert into incidents"
            + " (id, actor_role, incident_date, location_id, extracted_location_text,"
            + " confidence_score, confidence_level, political_accountability_link,"
            + " extraction_confidence, status, source_count, independent_publisher_count,"
            + " created_at, updated_at)"
            + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
        incidentId,
        row.actorRole(),
        row.date(),
        row.locationId(),
        row.extractedLocationText(),
        BigDecimal.valueOf(row.score()),
        row.confidenceLevel(),
        true,
        BigDecimal.valueOf(0.90),
        row.status(),
        sourceCount,
        sourceCount,
        now,
        now);

    insertTranslation(incidentId, "bn", row.bnTitle(), row.bnSummary());
    if (row.enTitle() != null && row.enSummary() != null) {
      insertTranslation(incidentId, "en", row.enTitle(), row.enSummary());
    }

    for (String category : row.categories()) {
      jdbc.update(
          "insert into incident_categories (incident_id, category_code) values (?,?)",
          incidentId,
          category);
    }

    for (int index = 0; index < row.publisherNames().size(); index++) {
      String publisherName = row.publisherNames().get(index);
      UUID publisherId = publishers.get(publisherName);
      String url = SOURCE_PREFIX + incidentId + "/source-" + (index + 1);
      jdbc.update(
          "insert into incident_sources"
              + " (id, incident_id, publisher_id, source_url, canonical_url, source_title,"
              + " relevant_excerpt, published_at, fetched_at, source_type, ai_relevance_score,"
              + " created_at)"
              + " values (?,?,?,?,?,?,?,?,?,?,?,?)",
          UUID.randomUUID(),
          incidentId,
          publisherId,
          url,
          url,
          row.enTitle() == null ? row.bnTitle() : row.enTitle(),
          row.bnSummary(),
          Timestamp.valueOf(row.date().atStartOfDay()),
          now,
          "NEWSPAPER",
          BigDecimal.valueOf(92),
          now);
    }
  }

  private void insertTranslation(
      UUID incidentId, String languageCode, String title, String summary) {
    jdbc.update(
        "insert into incident_translations (incident_id, language_code, title, summary)"
            + " values (?,?,?,?)",
        incidentId,
        languageCode,
        title,
        summary);
  }

  private UUID publisher(String name, String domain, String homepageUrl) {
    List<UUID> ids =
        jdbc.query(
            "select id from publishers where domain = ? limit 1",
            (rs, rowNum) -> rs.getObject("id", UUID.class),
            domain);
    if (!ids.isEmpty()) {
      return ids.getFirst();
    }

    UUID id = UUID.randomUUID();
    Timestamp now = Timestamp.from(Instant.now());
    jdbc.update(
        "insert into publishers"
            + " (id, name, type, domain, homepage_url, active, created_at, updated_at)"
            + " values (?,?,?,?,?,?,?,?)",
        id,
        name,
        "NEWSPAPER",
        domain,
        homepageUrl,
        true,
        now,
        now);
    return id;
  }

  private UUID location(String division, String district, String upazila) {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "insert into locations (id, country, division, district, upazila) values (?,?,?,?,?)",
        id,
        "Bangladesh",
        division,
        district,
        upazila);
    return id;
  }

  private SeedIncident incident(
      String actorRole,
      String status,
      int score,
      String confidenceLevel,
      UUID locationId,
      String extractedLocationText,
      LocalDate date,
      String bnTitle,
      String bnSummary,
      String enTitle,
      String enSummary,
      List<String> categories,
      List<String> publisherNames) {
    return new SeedIncident(
        actorRole,
        status,
        score,
        confidenceLevel,
        locationId,
        extractedLocationText,
        date,
        bnTitle,
        bnSummary,
        enTitle,
        enSummary,
        new ArrayList<>(categories),
        new ArrayList<>(publisherNames));
  }

  private record SeedIncident(
      String actorRole,
      String status,
      int score,
      String confidenceLevel,
      UUID locationId,
      String extractedLocationText,
      LocalDate date,
      String bnTitle,
      String bnSummary,
      String enTitle,
      String enSummary,
      List<String> categories,
      List<String> publisherNames) {}
}
