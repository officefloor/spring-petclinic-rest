package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for the owner telephone normalisation rule used across the
 * {@code POST /owners} pipeline: a telephone is canonicalised to E.164 form. Both
 * {@link NormalizeOwnerTelephone} (which stores the canonical value and rejects an input that
 * cannot form one) and {@link OwnerIdentity} (which folds the canonical value into the identity
 * key) must agree on this definition, so it lives here rather than being re-implemented at each
 * site.
 *
 * <p>The rule: strip spaces, dashes and brackets; keep a leading {@code '+'} and country code
 * when present, otherwise assume country code {@code +61} and drop a single leading {@code '0'}
 * from the national digits; the result must be a {@code '+'} followed by 8 to 15 digits. So
 * {@code "0412 345 678"} canonicalises to {@code "+61412345678"} and {@code "+64 21 123 456"}
 * to {@code "+6421123456"}.
 *
 * <p>In addition, where the country code has a known fixed national-number length, the national
 * part (the digits after the country code) must have exactly that length: {@code +61} requires
 * 9 national digits and {@code +1} requires 10. A number whose national part is the wrong length
 * for its country is not a valid E.164 number here and is rejected. Country codes without a
 * listed rule keep only the general 8-to-15-digit total check.
 */
final class OwnerTelephone {

    /**
     * Country code (without the leading {@code '+'}) to its required national-number length.
     * Ordered longest-prefix-first so a number is matched against {@code 61} before {@code 1}.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private OwnerTelephone() {
    }

    /**
     * Canonicalise {@code telephone} to E.164 form, or return {@code null} when it cannot form a
     * valid E.164 number (null/blank input, a non-digit left after stripping separators, fewer
     * than 8 or more than 15 digits after the {@code '+'}, or a national part whose length is
     * wrong for a country code with a known fixed length — {@code +61} => 9, {@code +1} => 10).
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        // Strip spaces, dashes and brackets, leaving an optional leading '+' and the digits.
        String cleaned = telephone.replaceAll("[\\s()\\[\\]-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Leading '+' and country code present: keep as-is.
            digits = cleaned.substring(1);
        } else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        // Enforce the per-country national-number length where one is known.
        for (Map.Entry<String, Integer> rule : NATIONAL_LENGTHS.entrySet()) {
            String countryCode = rule.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != rule.getValue()) {
                    return null;
                }
                break;
            }
        }
        return "+" + digits;
    }
}
