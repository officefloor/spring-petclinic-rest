package org.springframework.samples.petclinic.util;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Converts a raw telephone number to <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form: a leading '+' followed by 8 to 15 digits.
 *
 * <p>An explicit leading '+' and country code are kept; otherwise the default country code
 * {@code +61} is assumed and a single leading '0' is dropped from the national digits.
 * Spaces, dashes and brackets are stripped. So {@code "0412 345 678"} becomes
 * {@code "+61412345678"} and {@code "+64 21 123 456"} becomes {@code "+6421123456"}.
 */
public final class E164PhoneNumber {

    /** Country code assumed when the input does not carry an explicit '+' prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Separators removed before parsing: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    /** A well-formed E.164 number: '+' then 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("\\+[0-9]{8,15}");

    /**
     * Required national-number length (the digits after the country code) for the country
     * codes whose national number is a fixed length. Country codes not listed carry no
     * length rule here, so any national length within the generic E.164 bounds is accepted.
     */
    private static final Map<String, Integer> NATIONAL_NUMBER_LENGTHS = Map.of(
            "1", 10,   // North American Numbering Plan
            "61", 9);  // Australia

    private E164PhoneNumber() {
    }

    /**
     * @param raw the caller-supplied telephone, in any common format
     * @return the E.164 string, or empty when {@code raw} cannot form a valid one
     */
    public static Optional<String> toE164(String raw) {
        if (raw == null) {
            return Optional.empty();
        }
        String trimmed = raw.trim();
        boolean explicitCountryCode = trimmed.startsWith("+");
        String cleaned = SEPARATORS.matcher(trimmed).replaceAll("");
        String digits;
        if (explicitCountryCode) {
            digits = cleaned.substring(1); // drop the '+', keep the country code
        }
        else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        String candidate = "+" + digits;
        return E164.matcher(candidate).matches() ? Optional.of(candidate) : Optional.empty();
    }

    /**
     * Formats a stored E.164 number for humans: the country code, a space, then the national
     * digits grouped in threes. So {@code "+61412345678"} becomes {@code "+61 412 345 678"}.
     *
     * <p>The country code is the longest {@link #NATIONAL_NUMBER_LENGTHS known} code that
     * prefixes the number, reusing the same country-code facts as the length rule.
     *
     * @param e164 a normalized E.164 number (a '+' followed by digits), as produced by
     *   {@link #toE164}
     * @return the human-readable form, or {@code e164} unchanged when it is null, not a
     *   well-formed E.164 number, or carries no recognized country code (so the split between
     *   country code and national number is unknown)
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !E164.matcher(e164).matches()) {
            return e164;
        }
        String digits = e164.substring(1);
        Optional<String> countryCode = NATIONAL_NUMBER_LENGTHS.keySet().stream()
                .filter(digits::startsWith)
                .max(Comparator.comparingInt(String::length));
        if (countryCode.isEmpty()) {
            return e164;
        }
        String national = digits.substring(countryCode.get().length());
        return "+" + countryCode.get() + " " + groupInThrees(national);
    }

    /** Groups a run of digits into space-separated groups of three, left to right. */
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

    /**
     * Checks a normalized E.164 number's national-number length against its country code.
     *
     * @param e164 a normalized E.164 number (a '+' followed by digits), as produced by
     *   {@link #toE164}
     * @return {@code true} when the national number has the length its country code
     *   requires, or the country code has no fixed-length rule; {@code false} when the
     *   length is wrong for a country code with a known requirement
     */
    public static boolean hasValidNationalNumberLength(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return false;
        }
        String digits = e164.substring(1);
        return NATIONAL_NUMBER_LENGTHS.entrySet().stream()
                .filter(entry -> digits.startsWith(entry.getKey()))
                .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(entry -> digits.length() - entry.getKey().length() == entry.getValue())
                .orElse(true);
    }
}
