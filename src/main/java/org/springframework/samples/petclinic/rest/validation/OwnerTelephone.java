package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Normalizes and validates owner telephone numbers into E.164 form.
 */
public final class OwnerTelephone {

    public static final String DEFAULT_COUNTRY_CODE = "61";

    public static final int MIN_DIGITS = 8;

    public static final int MAX_DIGITS = 15;

    /** Matches a normalized E.164 number: '+', a non-zero leading digit, {@value #MIN_DIGITS}-{@value #MAX_DIGITS} digits in total. */
    public static final String E164_REGEXP = "^\\+[1-9][0-9]{7,14}$";

    private static final Pattern E164 = Pattern.compile(E164_REGEXP);

    /** Required national-number length (digits after the country code) for known country codes. */
    public static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = Map.of(
        "61", 9,
        "1", 10);

    /** Two-digit ITU-T E.164 country codes; '1' and '7' are single-digit, every other code has three digits. */
    private static final Set<String> TWO_DIGIT_COUNTRY_CODES = Set.of(
        "20", "27", "30", "31", "32", "33", "34", "36", "39", "40", "41", "43", "44", "45", "46", "47",
        "48", "49", "51", "52", "53", "54", "55", "56", "57", "58", "60", "61", "62", "63", "64", "65",
        "66", "81", "82", "84", "86", "90", "91", "92", "93", "94", "95", "98");

    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()\\[\\]]");

    private OwnerTelephone() {
    }

    /**
     * Converts a telephone number to E.164 form. Spaces, dashes and brackets are removed. A number
     * with a leading '+' keeps its country code; otherwise a single leading '0' is dropped and the
     * default country code '+{@value #DEFAULT_COUNTRY_CODE}' is prepended. For country codes listed in
     * {@link #NATIONAL_DIGITS_BY_COUNTRY_CODE} the national number must have exactly the required length.
     *
     * @param telephone the telephone number as supplied in the request
     * @return the E.164 telephone number, e.g. {@code +61412345678}
     * @throws InvalidTelephoneException if the number cannot form a valid E.164 number
     */
    public static String normalize(String telephone) {
        if (telephone == null) {
            throw new InvalidTelephoneException(null);
        }
        String compact = SEPARATORS.matcher(telephone).replaceAll("");
        String e164;
        if (compact.startsWith("+")) {
            e164 = compact;
        } else {
            String national = compact.startsWith("0") ? compact.substring(1) : compact;
            e164 = "+" + DEFAULT_COUNTRY_CODE + national;
        }
        if (!E164.matcher(e164).matches()) {
            throw new InvalidTelephoneException(telephone);
        }
        checkNationalLength(e164, telephone);
        return e164;
    }

    private static void checkNationalLength(String e164, String telephone) {
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> entry : NATIONAL_DIGITS_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int required = entry.getValue();
                if (digits.length() - countryCode.length() != required) {
                    throw new InvalidTelephoneException(telephone, "Telephone number with country code '+"
                        + countryCode + "' must have exactly " + required + " national digits");
                }
                return;
            }
        }
    }

    /**
     * Formats a stored E.164 telephone number for humans: '+', the country code, a space and the
     * national digits grouped in threes separated by spaces, where a trailing single digit joins the
     * preceding group, e.g. {@code +61412345678} becomes {@code +61 412 345 678} and
     * {@code +16085552654} becomes {@code +1 608 555 2654}.
     *
     * @param e164 the stored E.164 telephone number
     * @return the formatted number, or the input unchanged when it is not in E.164 form
     */
    public static String format(String e164) {
        if (e164 == null || !E164.matcher(e164).matches()) {
            return e164;
        }
        String digits = e164.substring(1);
        String countryCode = digits.substring(0, countryCodeLength(digits));
        String national = digits.substring(countryCode.length());
        StringBuilder display = new StringBuilder("+").append(countryCode);
        int start = 0;
        while (start < national.length()) {
            int remaining = national.length() - start;
            int end = remaining == 4 ? national.length() : start + Math.min(3, remaining);
            display.append(' ').append(national, start, end);
            start = end;
        }
        return display.toString();
    }

    private static int countryCodeLength(String digits) {
        char first = digits.charAt(0);
        if (first == '1' || first == '7') {
            return 1;
        }
        return TWO_DIGIT_COUNTRY_CODES.contains(digits.substring(0, 2)) ? 2 : 3;
    }
}
