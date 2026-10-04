package org.springframework.samples.petclinic.rest.validation;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a raw, user-supplied telephone number into E.164 form.
 *
 * <p>The rules are:
 * <ul>
 *     <li>spaces, dashes and brackets are stripped;</li>
 *     <li>a leading {@code '+'} and country code are kept when present;</li>
 *     <li>otherwise country code {@code '+61'} is assumed and a single leading
 *         {@code '0'} is dropped from the national digits;</li>
 *     <li>the result must carry 8 to 15 digits after the {@code '+'};</li>
 *     <li>for a known country code the national number (the digits after the
 *         country code) must have the exact length that country requires:
 *         {@code '+61'} requires 9 national digits and {@code '+1'} requires 10.</li>
 * </ul>
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and
 * {@code "+64 21 123 456"} becomes {@code "+6421123456"}. The normalization is
 * idempotent: feeding it an already-E.164 value returns that same value.
 */
public final class E164Telephone {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Known country codes mapped to the exact national-number length they require.
     * Ordered longest-prefix first so the country code is matched unambiguously.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private E164Telephone() {
    }

    /**
     * Normalize a raw telephone number to E.164 form, or return {@code null} when the
     * input is {@code null} or cannot form a valid E.164 number.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        // strip spaces, dashes and brackets
        String cleaned = raw.replaceAll("[\\s\\-()]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + cleaned;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                return nationalLength == entry.getValue() ? "+" + digits : null;
            }
        }
        return "+" + digits;
    }

    /**
     * Whether the raw value can be converted to a valid E.164 number.
     */
    public static boolean isValid(String raw) {
        return normalize(raw) != null;
    }
}
