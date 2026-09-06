package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Shared handling for the owner {@code telephone}. Numbers are stored in E.164 form:
 * a leading {@code '+'} and country code are kept when present; otherwise country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national
 * digits. Spaces, dashes and brackets are stripped, and the result must have 8 to 15
 * digits after the {@code '+'}.
 *
 * <p>Beyond that general range, the national number (the digits after the country code)
 * must have the exact length that country requires: {@code '+61'} takes 9 national
 * digits and {@code '+1'} takes 10. Other country codes are only bound by the general
 * 8-to-15 range.
 *
 * <p>So {@code '0412 345 678'} is stored as {@code '+61412345678'}.
 */
public final class Telephones {

    // Formatting characters allowed inside a supplied number: spaces, dashes and brackets.
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    // E.164: a '+' followed by 8 to 15 digits.
    private static final Pattern E164 = Pattern.compile("\\+\\d{8,15}");

    // Country code -> required national-number length. Ordered longest-prefix-first so a
    // '+61' number is matched against '+61' before the shorter '+1'.
    private static final Map<String, Integer> NATIONAL_DIGITS = new LinkedHashMap<>();

    static {
        NATIONAL_DIGITS.put("+61", 9);
        NATIONAL_DIGITS.put("+1", 10);
    }

    private Telephones() {
    }

    /**
     * Normalize a telephone to E.164 form.
     *
     * @throws InvalidTelephoneException when the value cannot form a valid E.164 number.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        String stripped = SEPARATORS.matcher(telephone == null ? "" : telephone).replaceAll("");
        String e164;
        if (stripped.startsWith("+")) {
            // Keep the leading '+' and the country code exactly as supplied.
            e164 = stripped;
        }
        else {
            // No country code: assume '+61' and drop a single leading '0' from the national digits.
            String national = stripped.startsWith("0") ? stripped.substring(1) : stripped;
            e164 = "+61" + national;
        }
        if (!E164.matcher(e164).matches()) {
            throw new InvalidTelephoneException(
                    "Telephone must form a valid E.164 number with 8 to 15 digits after the '+'");
        }
        // For a recognised country code, the national number must have exactly the length
        // that country requires (e.g. '+61' => 9 national digits, '+1' => 10).
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (e164.startsWith(code)) {
                int nationalLength = e164.length() - code.length();
                if (nationalLength != country.getValue()) {
                    throw new InvalidTelephoneException("Telephone with country code '" + code
                            + "' must have " + country.getValue() + " national digits");
                }
                break;
            }
        }
        return e164;
    }

    /**
     * Format a stored E.164 telephone for humans: the country code, a space, then the
     * national digits grouped in threes (e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}). A recognised country code ({@code '+61'}, {@code '+1'})
     * is kept as the prefix; otherwise the whole value after the {@code '+'} is grouped.
     * A {@code null} or non-E.164 value is returned unchanged.
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !E164.matcher(e164).matches()) {
            return e164;
        }
        for (String code : NATIONAL_DIGITS.keySet()) {
            if (e164.startsWith(code)) {
                return code + " " + groupInThrees(e164.substring(code.length()));
            }
        }
        return "+" + groupInThrees(e164.substring(1));
    }

    // Group a run of digits into space-separated groups of three, left to right.
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
