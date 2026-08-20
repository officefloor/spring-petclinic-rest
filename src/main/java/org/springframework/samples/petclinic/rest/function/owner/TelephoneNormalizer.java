package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Map;

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

    /**
     * Required national-number length (digits after the country code) per known country code.
     * {@code '+61'} (Australia) requires 9 national digits; {@code '+1'} (NANP) requires 10.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

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

    /**
     * Checks the national-number length of an E.164 value against its country code. For a known
     * country code ({@code '+61'} → 9 national digits, {@code '+1'} → 10) the national part must be
     * exactly that many digits; unknown country codes are left to the generic 8-to-15 rule.
     *
     * @param e164 an E.164 string ({@code '+'} followed by digits), as produced by {@link #toE164}
     * @return {@code true} when the national-number length is valid for the country code
     */
    static boolean hasValidNationalLength(String e164) {
        String digits = e164.substring(1);
        String code = null;
        for (String candidate : NATIONAL_LENGTHS.keySet()) {
            if (digits.startsWith(candidate) && (code == null || candidate.length() > code.length())) {
                code = candidate;
            }
        }
        if (code == null) {
            return true;
        }
        return digits.length() - code.length() == NATIONAL_LENGTHS.get(code);
    }
}
