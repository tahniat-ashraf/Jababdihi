/**
 * Categories that warrant a "severe" visual cue (left rail + triangle glyph
 * on cards; red flag in filters). These are physical-harm / life-threatening
 * categories — the visual severity here is editorial emphasis, NOT a moral
 * or legal judgment about any individual incident.
 */
export const SEVERE_CATEGORIES = new Set<string>([
  "KILLING",
  "SEXUAL_VIOLENCE",
  "MOB_VIOLENCE",
  "ABDUCTION_CONFINEMENT",
  "ARMED_THREAT_ATTACK"
]);

export function isSevereCategory(code: string): boolean {
  return SEVERE_CATEGORIES.has(code);
}

export function hasSevereCategory(codes: readonly string[]): boolean {
  return codes.some((c) => SEVERE_CATEGORIES.has(c));
}
