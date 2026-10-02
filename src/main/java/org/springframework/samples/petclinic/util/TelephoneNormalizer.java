package org.springframework.samples.petclinic.util;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts raw telephone input into canonical E.164 form.
 *
 * <p>The rules are:
 * <ul>
 *   <li>spaces, dashes and brackets are stripped;</li>
 *   <li>a leading {@code '+'} and country code are kept when present;</li>
 *   <li>otherwise the default country code {@code '+61'} is assumed and a single
 *       leading {@code '0'} is dropped from the national digits;</li>
 *   <li>the result must contain between 8 and 15 digits after the {@code '+'};</li>
 *   <li>for a recognised country code the national number (the digits after the
 *       country code) must have the exact length that country requires:
 *       {@code '+61'} requires 9 national digits and {@code '+1'} requires 10.</li>
 * </ul>
 *
 * <p>So {@code '0412 345 678'} becomes {@code '+61412345678'} and
 * {@code '+64 21 123 456'} becomes {@code '+6421123456'}.
 */
public final class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Known country codes mapped to the exact number of national digits they require.
     * Ordered longest-code-first so the most specific prefix wins when matching.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put(DEFAULT_COUNTRY_CODE, 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private TelephoneNormalizer() {
    }

    /**
     * Convert a raw telephone number to canonical E.164 form.
     *
     * @param raw the telephone number as supplied by the caller (any format)
     * @return the E.164 string (a leading {@code '+'} followed by 8-15 digits), or
     *         {@code null} if {@code raw} is {@code null}
     * @throws InvalidTelephoneException if {@code raw} cannot form a valid E.164 number
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        // Strip spaces, dashes and brackets.
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            // Keep the explicit country code.
            digits = cleaned.substring(1);
        } else {
            // Assume the default country code and drop a single leading '0'.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            throw new InvalidTelephoneException(raw);
        }
        assertNationalLength(digits, raw);
        return "+" + digits;
    }

    /**
     * Format a canonical E.164 number for human display: the country code (with its leading
     * {@code '+'}), a space, then the national digits grouped left-to-right in threes and
     * space-separated. For a recognised country code the national number is the digits after
     * that code (country codes are matched longest-first so the most specific prefix wins);
     * for an unrecognised code the whole digit sequence is grouped in threes. So
     * {@code '+61412345678'} becomes {@code '+61 412 345 678'}.
     *
     * @param e164 a canonical E.164 string (a leading {@code '+'} followed by 8-15 digits), or
     *             {@code null}
     * @return the display string, or {@code null} if {@code e164} is {@code null}
     */
    public static String toDisplay(String e164) {
        if (e164 == null) {
            return null;
        }
        String digits = e164.startsWith("+") ? e164.substring(1) : e164;
        for (String countryCode : NATIONAL_LENGTHS.keySet()) {
            if (digits.startsWith(countryCode) && digits.length() > countryCode.length()) {
                return "+" + countryCode + " " + groupInThrees(digits.substring(countryCode.length()));
            }
        }
        return "+" + groupInThrees(digits);
    }

    /**
     * Group a run of digits left-to-right into space-separated chunks of three (a trailing chunk
     * may have one or two digits). So {@code '412345678'} becomes {@code '412 345 678'}.
     */
    private static String groupInThrees(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Verify that, for a recognised country code, the national number (the digits following the
     * country code) has exactly the length that country requires. Country codes are matched
     * longest-first so the most specific prefix wins. Unrecognised country codes are left to the
     * generic 8-15 digit range check.
     *
     * @param digits the E.164 digits (no leading {@code '+'})
     * @param raw    the original caller input, used only for the rejection message
     * @throws InvalidTelephoneException if the national number has the wrong length for its country
     */
    private static void assertNationalLength(String digits, String raw) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                if (nationalLength != entry.getValue()) {
                    throw new InvalidTelephoneException(raw);
                }
                return;
            }
        }
    }
}
