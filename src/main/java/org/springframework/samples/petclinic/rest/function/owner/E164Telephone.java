package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts a raw telephone string to canonical E.164 form.
 *
 * <p>Rules:
 * <ul>
 *   <li>strip spaces, dashes and brackets;</li>
 *   <li>keep a leading {@code '+'} and country code when present;</li>
 *   <li>otherwise assume country code {@code '+61'} and drop a single leading {@code '0'}
 *       from the national digits;</li>
 *   <li>when the number carries an explicit country code, its national-number length must match
 *       that country: {@code '+61'} requires 9 national digits and {@code '+1'} requires 10;
 *       country codes outside this table fall back to the generic bound below;</li>
 *   <li>require 8 to 15 digits after the {@code '+'}.</li>
 * </ul>
 *
 * <p>So {@code "0412 345 678"} becomes {@code "+61412345678"} and {@code "+64 21 123 456"}
 * becomes {@code "+6421123456"}. A value that cannot form valid E.164 has no canonical form.
 */
public final class E164Telephone {

    /**
     * Required national-number digit count for each supported country code, longest code first so a
     * {@code "61…"} number is matched as {@code +61} rather than as some shorter code. Country codes
     * absent here are validated only by the generic 8-to-15 total-digit bound.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY_CODE = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("61", 9); // Australia
        NATIONAL_LENGTH_BY_COUNTRY_CODE.put("1", 10); // North American Numbering Plan
    }

    private E164Telephone() {
    }

    /**
     * @return the E.164 form of {@code raw}, or {@code null} if it cannot form a valid E.164 number.
     */
    public static String toE164OrNull(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
            // An explicit country code fixes the expected national-number length.
            if (!nationalLengthMatchesCountry(digits)) {
                return null;
            }
        } else {
            if (cleaned.startsWith("0")) {
                cleaned = cleaned.substring(1); // drop a single leading national-trunk '0'
            }
            digits = "61" + cleaned;
        }
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Formats a stored E.164 number for humans: the country code, a space, then the national digits
     * grouped in threes (left to right), e.g. {@code "+61412345678"} becomes {@code "+61 412 345 678"}.
     * The raw E.164 value is left untouched; this is a presentation-only rendering.
     *
     * @return the human-readable rendering of {@code e164}, or {@code null} when {@code e164} is not a
     *   value this normalizer produces — no leading {@code '+'}, not 8 to 15 digits, or a country code
     *   outside {@link #NATIONAL_LENGTH_BY_COUNTRY_CODE} whose national-number split is unknown.
     */
    public static String toDisplayOrNull(String e164) {
        if (e164 == null) {
            return null;
        }
        String cleaned = e164.replaceAll("[\\s()\\-]", "");
        if (!cleaned.startsWith("+")) {
            return null;
        }
        String digits = cleaned.substring(1);
        if (!digits.matches("[0-9]{8,15}")) {
            return null;
        }
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode) && digits.length() - countryCode.length() == entry.getValue()) {
                String national = digits.substring(countryCode.length());
                return "+" + countryCode + " " + groupInThrees(national);
            }
        }
        return null;
    }

    /** Groups a run of digits into space-separated chunks of three, left to right. */
    private static String groupInThrees(String national) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(national.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Checks the national-number length of an explicitly country-coded number against its country.
     *
     * @param digits the digits following the {@code '+'} (country code plus national number).
     * @return {@code false} only when {@code digits} begins with a supported country code and the
     *   remaining national digits are not the exact length that country requires; {@code true} for
     *   any country code outside the table (left to the generic length bound).
     */
    private static boolean nationalLengthMatchesCountry(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTH_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                return digits.length() - countryCode.length() == entry.getValue();
            }
        }
        return true;
    }
}
