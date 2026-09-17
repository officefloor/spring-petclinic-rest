package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Canonical telephone normalization shared by the create pipeline: {@link NormalizeTelephone} stores
 * the E.164 form, {@link IdentityKey} uses it for the telephone part of the duplicate key and
 * {@link ValidateTelephoneLength} checks its national-number length. Not a pipeline step, so it is
 * free to expose plain helpers.
 */
public final class TelephoneNormalizer {

    /** Minimum digits after the '+' for a valid E.164 number. */
    private static final int MIN_E164_DIGITS = 8;

    /** Maximum digits after the '+' for a valid E.164 number. */
    private static final int MAX_E164_DIGITS = 15;

    /** Default country code assumed when no leading '+' is present. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /**
     * Required national-number digit count per E.164 country code. Ordered longest code first so
     * {@link #hasValidNationalLength(String)} matches the most specific prefix (e.g. '61' before '1').
     */
    private static final Map<String, Integer> NATIONAL_LENGTH_BY_COUNTRY = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTH_BY_COUNTRY.put("61", 9); // Australia
        NATIONAL_LENGTH_BY_COUNTRY.put("1", 10); // NANP (e.g. US)
    }

    private TelephoneNormalizer() {
    }

    /**
     * Converts a raw telephone to canonical E.164 form, or returns {@code null} when it cannot form a
     * valid E.164 number. A leading '+' and country code are kept when present; otherwise the default
     * country code '+61' is assumed and a single leading '0' is dropped from the national digits.
     * Spaces, dashes and brackets (any non-digit) are stripped. The result must have 8 to 15 digits
     * after the '+'.
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + digits;
        }
        if (digits.length() < MIN_E164_DIGITS || digits.length() > MAX_E164_DIGITS) {
            return null;
        }
        return "+" + digits;
    }

    /** Size of each national-digit group in the human-readable display form. */
    private static final int DISPLAY_GROUP_SIZE = 3;

    /**
     * Formats a canonical E.164 number for humans as its country code, a space, then the national
     * digits grouped in threes (e.g. '+61412345678' -&gt; '+61 412 345 678'). The country code is
     * identified from {@link #NATIONAL_LENGTH_BY_COUNTRY} (longest match first); a number whose
     * country code has no configured rule simply has all its digits grouped after the '+'. Expects
     * the canonical form produced by {@link #toE164(String)}; a value not in that form is returned
     * unchanged.
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        for (String countryCode : NATIONAL_LENGTH_BY_COUNTRY.keySet()) {
            if (digits.startsWith(countryCode)) {
                return "+" + countryCode + " " + groupInThrees(digits.substring(countryCode.length()));
            }
        }
        return "+" + groupInThrees(digits);
    }

    /** Splits a run of digits into space-separated groups of {@link #DISPLAY_GROUP_SIZE}. */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % DISPLAY_GROUP_SIZE == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }

    /**
     * Whether an E.164 number's national-number length matches what its country code requires (e.g.
     * '+61' expects 9 national digits, '+1' expects 10). Numbers whose country code has no configured
     * rule are accepted, so this only rejects a wrong length for a known country. Expects the canonical
     * form produced by {@link #toE164(String)}.
     */
    public static boolean hasValidNationalLength(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return false;
        }
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> rule : NATIONAL_LENGTH_BY_COUNTRY.entrySet()) {
            String countryCode = rule.getKey();
            if (digits.startsWith(countryCode)) {
                return digits.length() - countryCode.length() == rule.getValue();
            }
        }
        return true;
    }
}
