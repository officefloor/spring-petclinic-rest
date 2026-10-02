package org.springframework.samples.petclinic.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Single source of truth for owner-telephone country-code knowledge and the two directions that depend
 * on it: the national-number length check used when normalizing an inbound number to E.164, and the
 * outbound formatting of a stored E.164 number for human display.
 *
 * <p>The {@link #NATIONAL_LENGTHS} table (country code to required national-number length) is the one
 * place the recognized codes live. The inbound normalizer
 * ({@code org.springframework.samples.petclinic.rest.function.owner.OwnerTelephone}) checks lengths
 * through {@link #hasValidNationalLength(String)}; {@link Owner#getTelephoneDisplay()} splits and groups
 * through {@link #toDisplay(String)}. Adding or changing a country here updates both directions at once.
 */
public final class TelephoneFormatter {

    /**
     * Country code (without the leading {@code '+'}) to its required national-number length. Iterated
     * longest-code-first (hence {@link LinkedHashMap}) so a longer code is matched before a shorter one
     * that is a prefix of the number. Country codes absent here are not length-checked and are not
     * recognized for display formatting. {@code +61} (Australia) has 9 national digits, {@code +1}
     * (North American Numbering Plan) has 10.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS;
    static {
        Map<String, Integer> lengths = new LinkedHashMap<>();
        lengths.put("61", 9); // Australia
        lengths.put("1", 10); // North American Numbering Plan
        NATIONAL_LENGTHS = lengths;
    }

    private TelephoneFormatter() {
    }

    /**
     * Whether the national-number length of {@code digits} (a {@code '+'}-less E.164 digit string)
     * matches its country code. Unrecognized country codes pass (they are length-checked only by the
     * general 8-to-15 digit rule enforced by the caller).
     */
    public static boolean hasValidNationalLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == entry.getValue();
            }
        }
        return true;
    }

    /**
     * The E.164 value {@code e164} formatted for humans: the country code (with its leading {@code '+'}),
     * a space, then the national digits grouped in threes (e.g. {@code +61412345678} ->
     * {@code +61 412 345 678}). Returned unchanged when {@code null} or not a recognized E.164 number
     * (a {@code '+'} and a known country code whose national-number length matches).
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() == entry.getValue()) {
                return "+" + code + " " + groupInThrees(digits.substring(code.length()));
            }
        }
        return e164;
    }

    /** Groups {@code digits} into space-separated runs of three, left to right. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i += 3) {
            if (grouped.length() > 0) {
                grouped.append(' ');
            }
            grouped.append(digits, i, Math.min(i + 3, digits.length()));
        }
        return grouped.toString();
    }
}
