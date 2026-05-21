package com.jababdihi.backend.seed;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Inserts deterministic staging data for smoke-testing the public feed.
 *
 * <p>Activated only when {@code --app.seed=staging} is passed on the command line. Idempotent:
 * skips if publishers are already present.
 *
 * <p>Run with:
 *
 * <pre>
 *   ./mvnw spring-boot:run -Dspring-boot.run.profiles=api,seed \
 *       -Dspring-boot.run.arguments="--app.seed=staging"
 * </pre>
 */
@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "staging")
class StagingDataSeeder implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(StagingDataSeeder.class);

  private final JdbcTemplate jdbc;

  StagingDataSeeder(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public void run(ApplicationArguments args) {
    Integer existing =
        jdbc.queryForObject(
            "SELECT COUNT(*) FROM publishers WHERE domain = 'prothomalo.com'", Integer.class);
    if (existing != null && existing > 0) {
      log.info("Staging data already present — skipping");
      System.exit(0);
      return;
    }

    log.info("Seeding staging data...");

    UUID pa = insertPublisher("Prothom Alo", "prothomalo.com", "https://www.prothomalo.com");
    UUID tds =
        insertPublisher("The Daily Star", "thedailystar.net", "https://www.thedailystar.net");
    UUID bd = insertPublisher("bdnews24", "bdnews24.com", "https://bdnews24.com");
    UUID dt = insertPublisher("Dhaka Tribune", "dhakatribune.com", "https://www.dhakatribune.com");
    UUID na = insertPublisher("New Age", "newagebd.net", "https://www.newagebd.net");
    List<UUID> pubs = List.of(pa, tds, bd, dt, na);

    UUID loc = insertLocation();

    insertRows(govPublished(loc), pubs);
    insertRows(oppPublished(loc), pubs);
    insertRows(nonPublic(loc), pubs);

    log.info("Staging seed complete — 31 incidents created");
    System.exit(0);
  }

  // ---- Seed data definitions ----

  private List<SeedRow> govPublished(UUID loc) {
    return List.of(
        // 1. EXTORTION + ABUSE_OF_POWER — HIGH — 5 sources → shows "+2 more"
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            82,
            loc,
            d(15),
            "স্থানীয় নেতার বিরুদ্ধে চাঁদাবাজির অভিযোগ",
            "একাধিক সংবাদমাধ্যমে স্থানীয় রাজনৈতিক সংশ্লিষ্ট ব্যক্তির বিরুদ্ধে"
                + " ব্যবসায়ীদের কাছ থেকে অর্থ দাবির অভিযোগ প্রকাশিত হয়েছে।",
            "Local leader alleged to extort businesses",
            "Multiple news outlets reported allegations that a politically affiliated local"
                + " leader demanded payments from business owners.",
            List.of("EXTORTION", "ABUSE_OF_POWER"),
            5),
        // 2. CORRUPTION_BRIBERY — HIGH — 4 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            78,
            loc,
            d(14),
            "কর্মকর্তার বিরুদ্ধে ঘুষ নেওয়ার অভিযোগ",
            "সূত্র অনুযায়ী, একজন সরকারি কর্মকর্তা অনুমতিপত্রের জন্য অবৈধ অর্থ দাবি"
                + " করেছেন বলে অভিযোগ রয়েছে।",
            "Official alleged to accept bribes for permits",
            "Sources reported allegations that a government official demanded unlawful payments"
                + " in exchange for permits.",
            List.of("CORRUPTION_BRIBERY"),
            4),
        // 3. POLITICAL_VIOLENCE — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            62,
            loc,
            d(13),
            "বিরোধী কর্মীদের ওপর হামলার অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক প্রতিদ্বন্দ্বী কর্মীদের ওপর হামলার অভিযোগ উঠেছে।",
            "Alleged attack on rival political activists",
            "Sources reported allegations of an attack on rival political activists.",
            List.of("POLITICAL_VIOLENCE"),
            3),
        // 4. KILLING — HIGH — 4 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            85,
            loc,
            d(12),
            "দলীয় সংশ্লিষ্ট ব্যক্তির হত্যার অভিযোগ",
            "একাধিক সংবাদমাধ্যম একজন দলীয় সংশ্লিষ্ট ব্যক্তির হত্যার অভিযোগ প্রকাশ করেছে।",
            "Alleged killing linked to local party figure",
            "Multiple sources reported allegations of a killing linked to a local party figure.",
            List.of("KILLING"),
            4),
        // 5. ABUSE_OF_POWER — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            55,
            loc,
            d(11),
            "প্রশাসনিক ক্ষমতার অপব্যবহারের অভিযোগ",
            "সূত্র অনুযায়ী, প্রশাসনিক পর্যায়ে ক্ষমতার অপব্যবহারের অভিযোগ রয়েছে।",
            "Alleged administrative abuse of power",
            "Sources reported allegations of abuse of power at the administrative level.",
            List.of("ABUSE_OF_POWER"),
            3),
        // 6. LAND_GRABBING — LOW — 2 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            30,
            loc,
            d(10),
            "জমি দখলের অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক সংশ্লিষ্টতার সাথে জমি দখলের অভিযোগ রয়েছে।",
            "Alleged land seizure with political links",
            "Sources reported alleged land seizure involving politically connected individuals.",
            List.of("LAND_GRABBING"),
            2),
        // 7. ARMED_THREAT_ATTACK — HIGH — 4 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            80,
            loc,
            d(9),
            "সশস্ত্র হুমকির অভিযোগ",
            "একাধিক সংবাদমাধ্যমে সশস্ত্র হুমকির ঘটনার অভিযোগ প্রকাশিত হয়েছে।",
            "Alleged armed threats reported",
            "Multiple news sources reported allegations of armed threats in the area.",
            List.of("ARMED_THREAT_ATTACK"),
            4),
        // 8. MOB_VIOLENCE — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            60,
            loc,
            d(8),
            "গণপিটুনির অভিযোগ",
            "সূত্র অনুযায়ী, দলীয় সংশ্লিষ্টতার সাথে গণপিটুনির ঘটনার অভিযোগ রয়েছে।",
            "Alleged mob violence incident",
            "Sources reported allegations of mob violence with reported political links.",
            List.of("MOB_VIOLENCE"),
            3),
        // 9. INTIMIDATION — LOW — 2 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            25,
            loc,
            d(7),
            "হুমকি ও ভয়ভীতির অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক উদ্দেশ্যে হুমকি ও ভয়ভীতির অভিযোগ রয়েছে।",
            "Alleged politically motivated threats",
            "Sources reported allegations of threats and intimidation with alleged political"
                + " motives.",
            List.of("INTIMIDATION"),
            2),
        // 10. EXTORTION — HIGH — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            75,
            loc,
            d(6),
            "ব্যবসায়ীদের কাছ থেকে চাঁদাবাজির অভিযোগ",
            "সূত্র অনুযায়ী, স্থানীয় ব্যবসায়ীদের কাছ থেকে চাঁদাবাজির অভিযোগ উঠেছে।",
            "Alleged extortion targeting local traders",
            "Sources reported allegations of extortion targeting local traders.",
            List.of("EXTORTION"),
            3),
        // 11. SEXUAL_VIOLENCE — HIGH — 5 sources → shows "+2 more"
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            88,
            loc,
            d(5),
            "যৌন সহিংসতার অভিযোগ",
            "একাধিক সংবাদমাধ্যমে দলীয় সংশ্লিষ্টতার সাথে যৌন সহিংসতার অভিযোগ প্রকাশিত" + " হয়েছে।",
            "Alleged sexual violence with party links",
            "Multiple news outlets reported allegations of sexual violence involving a"
                + " party-affiliated individual.",
            List.of("SEXUAL_VIOLENCE"),
            5),
        // 12. ABDUCTION_CONFINEMENT — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            65,
            loc,
            d(4),
            "অপহরণ ও আটকের অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক উদ্দেশ্যে অপহরণ ও অবৈধ আটকের অভিযোগ রয়েছে।",
            "Alleged abduction and unlawful confinement",
            "Sources reported allegations of abduction and unlawful confinement with alleged"
                + " political motives.",
            List.of("ABDUCTION_CONFINEMENT"),
            3),
        // 13. ATTACK_ON_INSTITUTION — HIGH — 5 sources → shows "+2 more"
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            90,
            loc,
            d(3),
            "শিক্ষা প্রতিষ্ঠানে হামলার অভিযোগ",
            "একাধিক সংবাদমাধ্যমে রাজনৈতিক সংশ্লিষ্টতার সাথে একটি শিক্ষা প্রতিষ্ঠানে"
                + " হামলার অভিযোগ প্রকাশিত হয়েছে।",
            "Alleged attack on educational institution",
            "Multiple sources reported allegations of an attack on an educational institution"
                + " with reported political links.",
            List.of("ATTACK_ON_INSTITUTION"),
            5),
        // 14. COMMUNAL_RELIGIOUS_VIOLENCE — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            58,
            loc,
            d(2),
            "সাম্প্রদায়িক সহিংসতার অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক সংশ্লিষ্টতার সাথে সাম্প্রদায়িক সহিংসতার অভিযোগ" + " রয়েছে।",
            "Alleged communal violence with political links",
            "Sources reported allegations of communal violence with reported political links.",
            List.of("COMMUNAL_RELIGIOUS_VIOLENCE"),
            3),
        // 15. CORRUPTION_BRIBERY + LAND_GRABBING — HIGH — 4 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            82,
            loc,
            d(1),
            "দুর্নীতি ও জমি দখলের অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক সংশ্লিষ্ট ব্যক্তির বিরুদ্ধে দুর্নীতি ও জমি দখলের"
                + " অভিযোগ রয়েছে।",
            "Alleged corruption and land seizure",
            "Sources reported allegations of corruption and land seizure involving a"
                + " party-affiliated individual.",
            List.of("CORRUPTION_BRIBERY", "LAND_GRABBING"),
            4),
        // 16. KILLING + POLITICAL_VIOLENCE — HIGH — 5 sources → shows "+2 more"
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            92,
            loc,
            LocalDate.of(2026, 2, 28),
            "দ্বিতীয় হত্যার অভিযোগ",
            "একাধিক সংবাদমাধ্যমে রাজনৈতিক সহিংসতায় হত্যার অভিযোগ প্রকাশিত হয়েছে।",
            "Second alleged politically linked killing",
            "Multiple sources reported allegations of a killing linked to political violence.",
            List.of("KILLING", "POLITICAL_VIOLENCE"),
            5),
        // 17. ELECTION_VIOLENCE — HIGH — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            70,
            loc,
            LocalDate.of(2026, 2, 27),
            "নির্বাচনী সহিংসতার অভিযোগ",
            "সূত্র অনুযায়ী, নির্বাচন প্রক্রিয়ায় বাধা দেওয়ার অভিযোগ রয়েছে।",
            "Alleged election violence and obstruction",
            "Sources reported allegations of violence and obstruction during the electoral"
                + " process.",
            List.of("ELECTION_VIOLENCE"),
            3),
        // 18. ABUSE_OF_POWER (police) — MODERATE — 2 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            48,
            loc,
            LocalDate.of(2026, 2, 26),
            "পুলিশের ক্ষমতার অপব্যবহারের অভিযোগ",
            "সূত্র অনুযায়ী, পুলিশ প্রশাসনের পর্যায়ে ক্ষমতার অপব্যবহারের অভিযোগ উঠেছে।",
            "Alleged police abuse of power",
            "Sources reported allegations of abuse of power by law enforcement officials.",
            List.of("ABUSE_OF_POWER"),
            2),
        // 19. EXTORTION + KILLING — HIGH — 4 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            84,
            loc,
            LocalDate.of(2026, 2, 25),
            "চাঁদাবাজি ও হত্যার অভিযোগ",
            "সূত্র অনুযায়ী, চাঁদাবাজিতে বাধা দেওয়ায় হত্যার অভিযোগ রয়েছে।",
            "Alleged extortion-linked killing",
            "Sources reported allegations of a killing linked to resistance against extortion.",
            List.of("EXTORTION", "KILLING"),
            4),
        // 20. MOB_VIOLENCE + COMMUNAL_RELIGIOUS_VIOLENCE — MODERATE — 3 sources
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            68,
            loc,
            LocalDate.of(2026, 2, 24),
            "দ্বিতীয় গণপিটুনির অভিযোগ",
            "সূত্র অনুযায়ী, সাম্প্রদায়িক উস্কানিতে গণপিটুনির অভিযোগ রয়েছে।",
            "Second alleged mob attack with communal links",
            "Sources reported allegations of a mob attack with reported communal incitement.",
            List.of("MOB_VIOLENCE", "COMMUNAL_RELIGIOUS_VIOLENCE"),
            3),
        // 21. DRUG_ARMS_CRIME — LOW — 2 sources (extra for pagination)
        row(
            "GOVERNMENT",
            "AUTO_PUBLISHED",
            38,
            loc,
            LocalDate.of(2026, 2, 23),
            "অবৈধ অস্ত্র সংশ্লিষ্ট অভিযোগ",
            "সূত্র অনুযায়ী, রাজনৈতিক সংশ্লিষ্টতার সাথে অবৈধ অস্ত্র ব্যবহারের অভিযোগ" + " রয়েছে।",
            "Alleged illegal arms with political links",
            "Sources reported allegations of illegal arms use with reported political links.",
            List.of("DRUG_ARMS_CRIME"),
            2));
  }

  private List<SeedRow> oppPublished(UUID loc) {
    return List.of(
        // 22. POLITICAL_VIOLENCE — HIGH — 4 sources
        row(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            75,
            loc,
            d(15),
            "বিরোধী দলের বিরুদ্ধে হামলার অভিযোগ",
            "সূত্র অনুযায়ী, বিরোধী দলের সাথে সংশ্লিষ্ট একজন ব্যক্তি হামলার সাথে জড়িত"
                + " বলে অভিযোগ রয়েছে।",
            "Alleged attack linked to Opposition figure",
            "Sources reported allegations that an Opposition-linked individual was involved in"
                + " an attack.",
            List.of("POLITICAL_VIOLENCE"),
            4),
        // 23. KILLING — HIGH — 5 sources → shows "+2 more"
        row(
            "OPPOSITION",
            "MANUALLY_PUBLISHED",
            82,
            loc,
            d(12),
            "বিরোধী দলের নেতার হত্যার অভিযোগ",
            "একাধিক সংবাদমাধ্যমে বিরোধী দলের একজন স্থানীয় নেতার হত্যার অভিযোগ প্রকাশিত"
                + " হয়েছে।",
            "Alleged killing of Opposition local leader",
            "Multiple news outlets reported allegations of the killing of a local Opposition"
                + " leader.",
            List.of("KILLING"),
            5),
        // 24. EXTORTION — MODERATE — 2 sources
        row(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            45,
            loc,
            d(9),
            "বিরোধী দলের চাঁদাবাজির অভিযোগ",
            "সূত্র অনুযায়ী, বিরোধী দলের সংশ্লিষ্ট ব্যক্তির বিরুদ্ধে চাঁদাবাজির অভিযোগ"
                + " রয়েছে।",
            "Opposition figure alleged to extort",
            "Sources reported allegations of extortion by an Opposition-affiliated figure.",
            List.of("EXTORTION"),
            2),
        // 25. CORRUPTION_BRIBERY — HIGH — 3 sources
        row(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            70,
            loc,
            d(6),
            "বিরোধী সংশ্লিষ্ট দুর্নীতির অভিযোগ",
            "সূত্র অনুযায়ী, বিরোধী দলের সাথে সংশ্লিষ্ট ব্যক্তির বিরুদ্ধে দুর্নীতির"
                + " অভিযোগ রয়েছে।",
            "Alleged corruption by Opposition-linked figure",
            "Sources reported allegations of corruption by an Opposition-linked individual.",
            List.of("CORRUPTION_BRIBERY"),
            3),
        // 26. ARMED_THREAT_ATTACK — HIGH — 4 sources
        row(
            "OPPOSITION",
            "AUTO_PUBLISHED",
            88,
            loc,
            d(3),
            "সশস্ত্র হামলার অভিযোগ",
            "একাধিক সংবাদমাধ্যমে বিরোধী দলের সাথে সংশ্লিষ্ট গোষ্ঠীর সশস্ত্র হামলার"
                + " অভিযোগ প্রকাশিত হয়েছে।",
            "Alleged armed attack by Opposition-linked group",
            "Multiple sources reported allegations of an armed attack by an Opposition-linked"
                + " group.",
            List.of("ARMED_THREAT_ATTACK"),
            4));
  }

  private List<SeedRow> nonPublic(UUID loc) {
    return List.of(
        // PENDING_REVIEW — GOVERNMENT — must not appear in public feed
        row(
            "GOVERNMENT",
            "PENDING_REVIEW",
            35,
            loc,
            d(16),
            "পর্যালোচনাধীন ঘটনা (সরকার)",
            "এই ঘটনাটি পর্যালোচনার অপেক্ষায় রয়েছে এবং সর্বজনীন ফিডে দৃশ্যমান নয়।",
            "Pending review — Government",
            "This incident is awaiting review and must not appear in the public feed.",
            List.of("EXTORTION"),
            2),
        // PENDING_REVIEW — UNKNOWN actor — must not appear
        row(
            "UNKNOWN",
            "PENDING_REVIEW",
            20,
            loc,
            d(16),
            "অজ্ঞাত অভিনেতার বিরুদ্ধে অভিযোগ",
            "অভিনেতার পরিচয় নিশ্চিত করা যায়নি।",
            "Allegation with unidentified actor",
            "Actor could not be identified — must not appear in the public feed.",
            List.of("POLITICAL_VIOLENCE"),
            2),
        // REJECTED — GOVERNMENT — must not appear
        row(
            "GOVERNMENT",
            "REJECTED",
            15,
            loc,
            d(17),
            "প্রত্যাখ্যাত ঘটনা",
            "এই ঘটনাটি সম্পাদকীয় পর্যালোচনায় প্রত্যাখ্যান করা হয়েছে।",
            "Rejected incident",
            "This incident was rejected during editorial review — must not appear in the public"
                + " feed.",
            List.of("INTIMIDATION"),
            2),
        // ARCHIVED — OPPOSITION — must not appear
        row(
            "OPPOSITION",
            "ARCHIVED",
            72,
            loc,
            d(17),
            "সংরক্ষণাগারে নেওয়া ঘটনা",
            "এই ঘটনাটি সক্রিয় ফিড থেকে সরানো হয়েছে।",
            "Archived incident",
            "This incident was removed from the active feed — must not appear publicly.",
            List.of("KILLING"),
            3),
        // PENDING_REVIEW — OPPOSITION — must not appear
        row(
            "OPPOSITION",
            "PENDING_REVIEW",
            42,
            loc,
            d(18),
            "পর্যালোচনাধীন বিরোধী দলের ঘটনা",
            "এই ঘটনাটি পর্যালোচনার অপেক্ষায় রয়েছে।",
            "Pending review — Opposition",
            "This incident is awaiting review — must not appear in the public feed.",
            List.of("CORRUPTION_BRIBERY"),
            2));
  }

  // ---- Helpers ----

  private void insertRows(List<SeedRow> rows, List<UUID> pubs) {
    for (SeedRow r : rows) {
      UUID id = UUID.randomUUID();
      Instant now = Instant.now();
      int pubCount = Math.min(r.numSources(), pubs.size());

      jdbc.update(
          "INSERT INTO incidents"
              + " (id, actor_role, incident_date, location_id, extracted_location_text,"
              + "  confidence_score, confidence_level, status, source_count,"
              + "  independent_publisher_count, created_at, updated_at)"
              + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
          id,
          r.actorRole(),
          r.date(),
          r.locationId(),
          "Mirpur, Dhaka",
          new BigDecimal(r.score()),
          scoreLabel(r.score()),
          r.status(),
          pubCount,
          pubCount,
          now,
          now);

      jdbc.update(
          "INSERT INTO incident_translations (incident_id, language_code, title, summary)"
              + " VALUES (?,?,?,?)",
          id,
          "bn",
          r.bnTitle(),
          r.bnSummary());
      jdbc.update(
          "INSERT INTO incident_translations (incident_id, language_code, title, summary)"
              + " VALUES (?,?,?,?)",
          id,
          "en",
          r.enTitle(),
          r.enSummary());

      for (int i = 0; i < pubCount; i++) {
        String url = "https://example.com/" + id + "/s" + i;
        jdbc.update(
            "INSERT INTO incident_sources"
                + " (id, incident_id, publisher_id, source_url, canonical_url, source_title,"
                + "  fetched_at, source_type, created_at)"
                + " VALUES (?,?,?,?,?,?,?,?,?)",
            UUID.randomUUID(),
            id,
            pubs.get(i),
            url,
            url,
            r.enTitle() + " — Source " + (i + 1),
            now,
            "NEWSPAPER",
            now);
      }

      for (String cat : r.categories()) {
        jdbc.update(
            "INSERT INTO incident_categories (incident_id, category_code) VALUES (?,?)", id, cat);
      }
    }
  }

  private UUID insertPublisher(String name, String domain, String homepageUrl) {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();
    jdbc.update(
        "INSERT INTO publishers"
            + " (id, name, type, domain, homepage_url, active, created_at, updated_at)"
            + " VALUES (?,?,?,?,?,?,?,?)",
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

  private UUID insertLocation() {
    UUID id = UUID.randomUUID();
    jdbc.update(
        "INSERT INTO locations (id, country, division, district, upazila) VALUES (?,?,?,?,?)",
        id,
        "Bangladesh",
        "Dhaka",
        "Dhaka",
        "Mirpur");
    return id;
  }

  private String scoreLabel(int score) {
    if (score >= 70) return "Highly Corroborated";
    if (score >= 40) return "Moderate Corroboration";
    return "Low Corroboration";
  }

  private LocalDate d(int dayOfMarch) {
    return LocalDate.of(2026, 3, dayOfMarch);
  }

  private SeedRow row(
      String actorRole,
      String status,
      int score,
      UUID locationId,
      LocalDate date,
      String bnTitle,
      String bnSummary,
      String enTitle,
      String enSummary,
      List<String> categories,
      int numSources) {
    return new SeedRow(
        actorRole,
        status,
        score,
        locationId,
        date,
        bnTitle,
        bnSummary,
        enTitle,
        enSummary,
        categories,
        numSources);
  }

  private record SeedRow(
      String actorRole,
      String status,
      int score,
      UUID locationId,
      LocalDate date,
      String bnTitle,
      String bnSummary,
      String enTitle,
      String enSummary,
      List<String> categories,
      int numSources) {}
}
