package org.springframework.samples.petclinic.model;

import java.util.Map;

/**
 * E.164 telephone-number metadata and human-readable formatting. Holds the known country codes and
 * the national-number length each requires, and renders a stored compact E.164 number for people to
 * read: the country code, a space, then the national digits grouped in threes (so
 * {@code "+61412345678"} becomes {@code "+61 412 345 678"}).
 *
 * <p>It is the single source of truth for E.164 country-code knowledge, shared by the request-time
 * normalization that produces the compact number and by the {@link Owner#getTelephoneDisplay()
 * display} derived from it.
 */
public final class E164 {

    /** Country code (digits, no {@code '+'}) to the exact national-number length it requires. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    /** National digits are shown in groups of this many, separated by spaces. */
    private static final int GROUP_SIZE = 3;

    private E164() {
    }

    /**
     * The national-number length required for {@code countryCode}, or {@code null} when no rule is
     * known for it.
     */
    public static Integer nationalLength(String countryCode) {
        return NATIONAL_LENGTHS.get(countryCode);
    }

    /**
     * The known country code that prefixes {@code digits} (the E.164 body: country code plus national
     * number, no {@code '+'}), taken as the longest matching known country code, or {@code null} when
     * none is known.
     */
    public static String countryCodeOf(String digits) {
        String match = null;
        for (String code : NATIONAL_LENGTHS.keySet()) {
            if (digits.startsWith(code) && (match == null || code.length() > match.length())) {
                match = code;
            }
        }
        return match;
    }

    /**
     * Formats a compact E.164 number for humans as the country code, a space, then the national
     * digits grouped in threes (e.g. {@code "+61412345678"} to {@code "+61 412 345 678"}). Returns
     * the input unchanged when it is {@code null}, not {@code '+'}-prefixed, or carries a country code
     * with no known length (so it cannot be split reliably).
     */
    public static String display(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String countryCode = countryCodeOf(digits);
        if (countryCode == null) {
            return e164;
        }
        return "+" + countryCode + " " + groupInThrees(digits.substring(countryCode.length()));
    }

    /** Groups {@code national} into runs of {@link #GROUP_SIZE} digits separated by single spaces. */
    private static String groupInThrees(String national) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return grouped.toString();
    }
}
