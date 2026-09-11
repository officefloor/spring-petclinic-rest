package org.springframework.samples.petclinic.model;

import java.util.List;

/**
 * Formats a stored E.164 telephone number for human reading: the country code, a single
 * space, then the national digits grouped in threes (e.g. {@code "+61412345678"} becomes
 * {@code "+61 412 345 678"}). The raw {@code telephone} field keeps its E.164 form; this
 * derives only the display variant.
 *
 * <p>The country code is taken from the known calling codes ({@code +61} Australia,
 * {@code +1} NANP), matching the same set {@code TelephoneE164} uses when normalizing.
 * An E.164 number with an unknown country code falls back to a single-digit code, and a
 * value that is not in E.164 form (null, empty, or missing the leading {@code '+'})
 * yields {@code null}.
 */
public final class TelephoneDisplay {

    /** Known country calling codes, longest first so the longest prefix wins. */
    private static final List<String> COUNTRY_CODES = List.of("61", "1");

    private TelephoneDisplay() {
    }

    /** The human display form of the owner's stored telephone, or {@code null} when absent. */
    public static String of(Owner owner) {
        return owner == null ? null : format(owner.getTelephone());
    }

    /**
     * Formats an E.164 number as {@code "+<code> <national grouped in threes>"}, or returns
     * {@code null} when {@code e164} is not a {@code '+'}-prefixed run of digits.
     */
    public static String format(String e164) {
        if (e164 == null || e164.isEmpty() || e164.charAt(0) != '+') {
            return null;
        }
        String digits = e164.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return null;
        }
        String code = countryCode(digits);
        String national = digits.substring(code.length());
        StringBuilder sb = new StringBuilder("+").append(code);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /** The country calling code prefixing {@code digits}, defaulting to a single digit. */
    private static String countryCode(String digits) {
        for (String code : COUNTRY_CODES) {
            if (digits.startsWith(code)) {
                return code;
            }
        }
        return digits.substring(0, 1);
    }
}
