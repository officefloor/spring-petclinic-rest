package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Converts a raw telephone into E.164 form: a leading '+' and country code are kept when
 * present, otherwise country code '+61' is assumed and a single leading '0' is dropped from
 * the national digits. Spaces, dashes and brackets (indeed every non-digit character) are
 * stripped, and the result must carry 8 to 15 digits after the '+'.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class E164Telephone {

    private E164Telephone() {
    }

    /**
     * @return the E.164 form ('+' followed by 8 to 15 digits).
     * @throws InvalidTelephoneException if the input cannot form valid E.164.
     */
    public static String normalize(String raw) throws InvalidTelephoneException {
        if (raw == null) {
            throw new InvalidTelephoneException("Telephone is required");
        }
        boolean hasCountryCode = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            throw new InvalidTelephoneException(
                    "Telephone must have 8 to 15 digits in E.164 form");
        }
        return "+" + digits;
    }

    /**
     * Validates the national-number length of an E.164 telephone against its country code:
     * country code '+61' (Australia) requires exactly 9 national digits and '+1' (NANP)
     * requires exactly 10. Country codes without a pinned rule keep only the generic 8 to 15
     * digit bound enforced by {@link #normalize}.
     *
     * @param e164 an E.164 telephone as produced by {@link #normalize} ('+' then 8 to 15 digits).
     * @throws InvalidTelephoneException if the national-number length is wrong for the country.
     */
    public static void validateNationalNumberLength(String e164) throws InvalidTelephoneException {
        String digits = e164 == null ? "" : e164.replaceAll("\\D", "");
        CountryCode country = CountryCode.of(digits);
        if (country != null) {
            requireNationalLength(digits.length() - country.digits.length(),
                    country.nationalNumberLength, country.dialCode());
        }
    }

    private static void requireNationalLength(int actual, int expected, String countryCode)
            throws InvalidTelephoneException {
        if (actual != expected) {
            throw new InvalidTelephoneException("Telephone with country code " + countryCode
                    + " must have " + expected + " national digits");
        }
    }

    /**
     * Formats a stored E.164 telephone for humans: the '+' and country dial code, a space, then
     * the national digits grouped in threes and separated by spaces
     * (e.g. {@code "+61412345678"} -> {@code "+61 412 345 678"}). The country code is recognised
     * via the same pinned dial codes used for validation; when none is recognised (or the input
     * is not E.164) the value is returned unchanged. Returns {@code null} when the input is null.
     *
     * @param e164 an E.164 telephone as produced by {@link #normalize} ('+' then 8 to 15 digits).
     * @return the human-facing display form, or the input unchanged when it is not recognised E.164.
     */
    public static String display(String e164) {
        if (e164 == null) {
            return null;
        }
        String digits = e164.replaceAll("\\D", "");
        CountryCode country = CountryCode.of(digits);
        if (country == null || digits.length() <= country.digits.length()) {
            return e164;
        }
        String national = digits.substring(country.digits.length());
        StringBuilder sb = new StringBuilder("+").append(country.digits);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /** E.164 form of a telephone, or {@code null} when it is absent or cannot be formed. */
    public static String normalizeOrNull(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            return normalize(raw);
        }
        catch (InvalidTelephoneException ex) {
            return null;
        }
    }

    /**
     * The known E.164 country codes and the national-number length pinned for each: '+61'
     * (Australia) requires 9 national digits, '+1' (NANP) requires 10. A country code is
     * recognised when its bare dial digits lead the E.164 number's digits; splitting the dial
     * code off leaves the national number. Country codes with no entry here keep only the
     * generic 8-to-15 digit bound from {@link #normalize}.
     */
    private enum CountryCode {

        /** Australia: '+61', 9 national digits. */
        AUSTRALIA("61", 9),

        /** North American Numbering Plan: '+1', 10 national digits. */
        NANP("1", 10);

        /** Bare dial-code digits, matched as a prefix of an E.164 number's digits. */
        private final String digits;

        /** Exact national-number length required for this country code. */
        private final int nationalNumberLength;

        CountryCode(String digits, int nationalNumberLength) {
            this.digits = digits;
            this.nationalNumberLength = nationalNumberLength;
        }

        /**
         * The recognised country code whose dial digits lead {@code e164Digits}, or
         * {@code null} when none is recognised. Codes are tried in declaration order, so a
         * longer dial code takes precedence over a shorter one that shares its lead.
         */
        static CountryCode of(String e164Digits) {
            for (CountryCode country : values()) {
                if (e164Digits.startsWith(country.digits)) {
                    return country;
                }
            }
            return null;
        }

        /** The '+'-prefixed dial code, e.g. {@code "+61"}. */
        String dialCode() {
            return "+" + digits;
        }
    }
}
