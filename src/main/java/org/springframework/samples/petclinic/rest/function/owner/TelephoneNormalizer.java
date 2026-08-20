package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Converts a raw telephone entry into E.164 form.
 *
 * <p>Rules: strip spaces, dashes and brackets; keep a leading {@code '+'} and its country
 * code when present; otherwise assume country code {@code '+61'} and drop a single leading
 * {@code '0'} from the national digits. The result is {@code '+'} followed by 8 to 15 digits;
 * anything that cannot form a valid E.164 number yields {@code null}.
 *
 * <p>The conversion is idempotent — feeding an already-E.164 value back in returns it
 * unchanged — so it can normalize both incoming requests and already-stored values for
 * comparison.
 */
final class TelephoneNormalizer {

    private TelephoneNormalizer() {
    }

    /**
     * @param raw the raw telephone text (may be {@code null})
     * @return the E.164 string, or {@code null} when {@code raw} cannot form a valid one
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }
}
