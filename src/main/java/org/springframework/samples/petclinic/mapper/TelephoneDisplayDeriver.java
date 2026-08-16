package org.springframework.samples.petclinic.mapper;

/**
 * Formats an owner's stored E.164 telephone for human display.
 *
 * <p>{@link #telephoneDisplay(String)} keeps the raw E.164 value untouched and
 * produces a grouped rendering: the {@code '+'} and country code, a space, then
 * the national digits split into groups of three separated by spaces
 * (e.g. {@code '+61412345678'} -> {@code '+61 412 345 678'}). The country code is
 * the leading digits recognised for the supported countries ({@code '61'},
 * {@code '1'}); anything else falls back to a single-digit country code so a
 * best-effort grouping is still produced.
 *
 * <p>Kept as a standalone helper (rather than a method on {@link OwnerMapper}) so
 * MapStruct does not mistake it for a generic {@code String -> String} mapping
 * method and apply it to unrelated fields; the mapper references it only through
 * an explicit expression.
 */
public final class TelephoneDisplayDeriver {

    /** Country codes recognised when splitting an E.164 number into code + national digits. */
    private static final String[] COUNTRY_CODES = {"61", "1"};

    private TelephoneDisplayDeriver() {
    }

    /**
     * Returns the E.164 {@code telephone} formatted as {@code '+<code> <ddd> <ddd> ...'}: the
     * country code, a space, and the national digits grouped in threes. Returns the input
     * unchanged when it is {@code null}, blank, or not a {@code '+'}-prefixed run of digits.
     */
    public static String telephoneDisplay(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return telephone;
        }
        if (!telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = countryCode(digits);
        String national = digits.substring(code.length());
        StringBuilder sb = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /** Returns the recognised country code prefixing {@code digits}, or its first digit otherwise. */
    private static String countryCode(String digits) {
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code) && digits.length() > code.length()) {
                return code;
            }
        }
        return digits.substring(0, 1);
    }
}
