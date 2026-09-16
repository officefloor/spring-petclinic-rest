package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Shared E.164 telephone knowledge: the recognized country codes and how to present a
 * stored E.164 number to humans.
 *
 * <p>The country-code table is the single source of truth for both normalization (see
 * {@code TelephoneNormalizer}, which validates the national-number length against it)
 * and display formatting ({@link #display(String)}, which uses it to split the country
 * code from the national digits).
 */
public final class Telephone {

    /** Country code (without '+') -> the exact number of national digits E.164 requires. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    private Telephone() {
    }

    /**
     * @return the longest recognized country code that {@code digits} (an E.164 number
     *         without its leading '+') begins with, or {@code null} when none is
     *         recognized.
     */
    public static String countryCode(String digits) {
        String match = null;
        for (String code : NATIONAL_LENGTHS.keySet()) {
            if (digits.startsWith(code) && (match == null || code.length() > match.length())) {
                match = code;
            }
        }
        return match;
    }

    /**
     * @return the exact number of national digits E.164 requires for the recognized
     *         country {@code code}, or {@code null} when the code is not recognized.
     */
    public static Integer nationalLength(String code) {
        return NATIONAL_LENGTHS.get(code);
    }

    /**
     * Formats a stored E.164 number (a leading '+' followed by 8 to 15 digits) for
     * humans: the country code, a space, then the national digits grouped left-to-right
     * in threes, e.g. {@code '+61412345678'} -> {@code '+61 412 345 678'}. An
     * unrecognized country code is not split out; all digits are grouped as national.
     *
     * @return the human-readable form, or {@code null} when {@code e164} is {@code null}.
     */
    public static String display(String e164) {
        if (e164 == null) {
            return null;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        String code = countryCode(digits);
        String national = (code == null) ? digits : digits.substring(code.length());
        String grouped = groupInThrees(national);
        return (code == null) ? "+" + grouped : "+" + code + " " + grouped;
    }

    /** Groups the digits left-to-right in threes, space-separated. */
    private static String groupInThrees(String digits) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                builder.append(' ');
            }
            builder.append(digits.charAt(i));
        }
        return builder.toString();
    }
}
