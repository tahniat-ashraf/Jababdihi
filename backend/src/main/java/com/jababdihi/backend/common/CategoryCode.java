package com.jababdihi.backend.common;

import java.util.Arrays;
import java.util.List;

public enum CategoryCode {
  POLITICAL_VIOLENCE("Political Violence", "রাজনৈতিক সহিংসতা", true),
  KILLING("Killing / Murder", "হত্যা / খুন", true),
  MOB_VIOLENCE("Mob Violence / Lynching", "গণপিটুনি / জনতার সহিংসতা", true),
  SEXUAL_VIOLENCE("Sexual Violence", "যৌন সহিংসতা", true),
  CORRUPTION_BRIBERY("Corruption / Bribery", "দুর্নীতি / ঘুষ", true),
  EXTORTION("Extortion / Chanda", "চাঁদাবাজি", true),
  LAND_GRABBING("Land Grabbing / Property Capture", "জমি দখল / সম্পত্তি দখল", true),
  ARMED_THREAT_ATTACK("Armed Threat / Attack", "সশস্ত্র হুমকি / হামলা", true),
  ABDUCTION_CONFINEMENT("Abduction / Confinement", "অপহরণ / আটক", true),
  ROBBERY_MUGGING("Robbery / Mugging", "ডাকাতি / ছিনতাই", false),
  INTIMIDATION("Threat / Intimidation", "হুমকি / ভয়ভীতি", true),
  ABUSE_OF_POWER("Abuse of Power", "ক্ষমতার অপব্যবহার", true),
  ATTACK_ON_INSTITUTION("Attack on Institution", "প্রতিষ্ঠানে হামলা", true),
  COMMUNAL_RELIGIOUS_VIOLENCE(
      "Communal / Religious Violence", "সাম্প্রদায়িক / ধর্মীয় সহিংসতা", true),
  DRUG_ARMS_CRIME("Drug / Illegal Arms Nexus", "মাদক / অবৈধ অস্ত্র সংশ্লিষ্ট অপরাধ", false),
  ELECTION_VIOLENCE("Election Violence / Obstruction", "নির্বাচন সহিংসতা / বাধা", false);

  private final String labelEn;
  private final String labelBn;
  private final boolean defaultVisible;

  CategoryCode(String labelEn, String labelBn, boolean defaultVisible) {
    this.labelEn = labelEn;
    this.labelBn = labelBn;
    this.defaultVisible = defaultVisible;
  }

  public String label(LanguageCode language) {
    return language == LanguageCode.BN ? labelBn : labelEn;
  }

  public String labelEn() {
    return labelEn;
  }

  public String labelBn() {
    return labelBn;
  }

  public boolean defaultVisible() {
    return defaultVisible;
  }

  public static List<CategoryCode> all() {
    return Arrays.asList(values());
  }
}
