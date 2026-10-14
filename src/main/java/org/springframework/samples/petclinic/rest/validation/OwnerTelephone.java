package org.springframework.samples.petclinic.rest.validation;

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

    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()\\[\\]]");

    private OwnerTelephone() {
    }

    /**
     * Converts a telephone number to E.164 form. Spaces, dashes and brackets are removed. A number
     * with a leading '+' keeps its country code; otherwise a single leading '0' is dropped and the
     * default country code '+{@value #DEFAULT_COUNTRY_CODE}' is prepended.
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
        return e164;
    }
}
