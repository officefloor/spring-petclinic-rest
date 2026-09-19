package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * E.164 telephone knowledge shared across the application: the country calling codes it
 * recognizes, the national-number length each requires, and human-readable display formatting.
 * <p>
 * This is deliberately separate from the lenient canonicalization that turns a raw request
 * value into E.164 (an owner-pipeline concern): canonicalization stays lenient so numbers can
 * be compared against existing owners, while the create request is additionally held to the
 * stricter, country-specific national length checked here.
 */
public final class Telephones {

    /** Country calling code -> required national-number length. Matched longest-code-first,
     *  so a code that is a prefix of another still resolves to the more specific rule. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of(
            "61", 9,
            "1", 10);

    private Telephones() {
    }

    /**
     * The longest recognized country calling code that prefixes the digits of {@code e164}
     * (a '+' followed by digits), or {@code null} when no configured code matches or the
     * argument is not in E.164 form.
     */
    public static String countryCode(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return null;
        }
        String digits = e164.substring(1);
        String best = null;
        for (String code : NATIONAL_DIGITS.keySet()) {
            if (digits.startsWith(code) && (best == null || code.length() > best.length())) {
                best = code;
            }
        }
        return best;
    }

    /**
     * Returns {@code true} when {@code e164} (a '+' followed by digits) carries the
     * national-number length required for its country code, or when its country code has no
     * configured length. A {@code null} or non-E.164 argument returns {@code false}.
     */
    public static boolean isNationalLengthValid(String e164) {
        String code = countryCode(e164);
        if (code == null) {
            return e164 != null && e164.startsWith("+");
        }
        String national = e164.substring(1 + code.length());
        return national.length() == NATIONAL_DIGITS.get(code);
    }

    /**
     * Format a stored E.164 telephone for humans as '+&lt;countryCode&gt; &lt;national digits
     * grouped in threes&gt;' (e.g. '+61412345678' becomes '+61 412 345 678'). When the country
     * code is not recognized, every digit after the '+' is grouped as the national part.
     * Returns {@code null} for a {@code null} or non-E.164 argument.
     */
    public static String forDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return null;
        }
        String code = countryCode(e164);
        String national = e164.substring(code == null ? 1 : 1 + code.length());
        String prefix = code == null ? "+" : "+" + code + " ";
        return prefix + groupInThrees(national);
    }

    /** Groups {@code digits} into space-separated runs of three from the left. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }
}
