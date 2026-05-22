import type { LanguageCode } from "@/lib/public-api";

const ACTOR_ROLE: Record<string, Record<LanguageCode, string>> = {
  GOVERNMENT: { en: "Government", bn: "সরকার" },
  OPPOSITION: { en: "Opposition", bn: "বিরোধী দল" },
  UNKNOWN: { en: "Unknown", bn: "অজ্ঞাত" }
};

const CATEGORIES: Record<string, Record<LanguageCode, string>> = {
  EXTORTION: { en: "Extortion", bn: "চাঁদাবাজি" },
  ABUSE_OF_POWER: { en: "Abuse of Power", bn: "ক্ষমতার অপব্যবহার" },
  POLITICAL_VIOLENCE: { en: "Political Violence", bn: "রাজনৈতিক সহিংসতা" },
  CORRUPTION_BRIBERY: { en: "Corruption / Bribery", bn: "দুর্নীতি / ঘুষ" },
  KILLING: { en: "Killing", bn: "হত্যা / খুন" },
  LAND_GRABBING: { en: "Land Grabbing", bn: "জমি দখল / সম্পত্তি দখল" },
  ARMED_THREAT_ATTACK: { en: "Armed Threat / Attack", bn: "সশস্ত্র হুমকি / হামলা" },
  MOB_VIOLENCE: { en: "Mob Violence", bn: "গণপিটুনি / জনতার সহিংসতা" },
  INTIMIDATION: { en: "Intimidation", bn: "হুমকি / ভয়ভীতি" },
  SEXUAL_VIOLENCE: { en: "Sexual Violence", bn: "যৌন সহিংসতা" },
  ABDUCTION_CONFINEMENT: { en: "Abduction / Confinement", bn: "অপহরণ / আটক" },
  ATTACK_ON_INSTITUTION: { en: "Attack on Institution", bn: "প্রতিষ্ঠানে হামলা" },
  COMMUNAL_RELIGIOUS_VIOLENCE: {
    en: "Communal / Religious Violence",
    bn: "সাম্প্রদায়িক / ধর্মীয় সহিংসতা"
  },
  ELECTION_VIOLENCE: { en: "Election Violence", bn: "নির্বাচনী সহিংসতা" },
  DRUG_ARMS_CRIME: { en: "Drug / Arms Crime", bn: "মাদক / অস্ত্র অপরাধ" }
};

const CONFIDENCE: Record<string, Record<LanguageCode, string>> = {
  "Highly Corroborated": { en: "Highly Corroborated", bn: "উচ্চ সমর্থিত" },
  Corroborated: { en: "Corroborated", bn: "সমর্থিত" },
  "Moderate Corroboration": { en: "Moderate Corroboration", bn: "মধ্যম সমর্থন" },
  "Limited Corroboration": { en: "Limited Corroboration", bn: "সীমিত সমর্থন" },
  "Low Corroboration": { en: "Low Corroboration", bn: "কম সমর্থন" }
};

const PUBLISHERS: Record<string, string> = {
  "Prothom Alo": "প্রথম আলো",
  bdnews24: "বিডিনিউজ২৪",
  "Dhaka Tribune": "ঢাকা ট্রিবিউন"
};

export const UI_COPY: Record<
  LanguageCode,
  { confidence: string; dateFallback: string }
> = {
  en: { confidence: "Confidence", dateFallback: "No date" },
  bn: { confidence: "আস্থা", dateFallback: "তারিখ নেই" }
};

export function localizeActorRole(code: string, language: LanguageCode): string {
  return ACTOR_ROLE[code]?.[language] ?? code;
}

export function localizeCategory(code: string, language: LanguageCode): string {
  return CATEGORIES[code]?.[language] ?? prettifyCode(code);
}

export function localizeConfidence(
  label: string | null | undefined,
  language: LanguageCode
): string | null {
  if (!label) return null;
  return CONFIDENCE[label]?.[language] ?? label;
}

export function localizePublisher(
  name: string | null | undefined,
  language: LanguageCode
): string | null {
  if (!name) return null;
  if (language === "bn") return PUBLISHERS[name] ?? name;
  return name;
}

export function formatIncidentDate(
  dateStr: string | null | undefined,
  language: LanguageCode
): string | null {
  if (!dateStr) return null;
  try {
    return new Intl.DateTimeFormat(language === "bn" ? "bn-BD" : "en-GB", {
      year: "numeric",
      month: "short",
      day: "numeric",
      timeZone: "UTC"
    }).format(new Date(dateStr));
  } catch {
    return dateStr;
  }
}

function prettifyCode(code: string): string {
  return code
    .split("_")
    .map((w) => w.charAt(0) + w.slice(1).toLowerCase())
    .join(" ");
}
