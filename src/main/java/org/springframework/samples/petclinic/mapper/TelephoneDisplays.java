package org.springframework.samples.petclinic.mapper;

import java.util.Map;

/**
 * Formats a stored E.164 telephone for human display: the '+'-prefixed country code,
 * a space, then the national digits grouped in threes (e.g. {@code +61412345678} ->
 * {@code +61 412 345 678}). The raw {@code telephone} field stays E.164; this is a
 * derived, read-only presentation of the same number.
 *
 * <p>The country-code boundary is taken from a small table of recognised codes (the
 * same ones {@code TelephoneE164} normalizes to); their national lengths do not overlap
 * as prefixes, so the split is unambiguous. An unrecognised code falls back to a
 * single-digit country code, still yielding a spaced, three-grouped rendering. Kept as
 * a plain static helper (not a mapper method) so MapStruct does not treat it as an
 * implicit String-to-String mapping method.
 */
public final class TelephoneDisplays {

    /** Recognised country calling codes; longest-prefix wins so overlaps stay unambiguous. */
    private static final Map<String, Integer> COUNTRY_CODE_LENGTHS = Map.of(
            "61", 2,
            "1", 1);

    private TelephoneDisplays() {
    }

    /**
     * The human-readable form of a stored E.164 number, or {@code null} when the input
     * is null/blank or is not a '+'-prefixed run of digits.
     */
    public static String of(String e164) {
        if (e164 == null) {
            return null;
        }
        String trimmed = e164.trim();
        if (!trimmed.startsWith("+") || !trimmed.substring(1).matches("\\d+")) {
            return null;
        }
        String digits = trimmed.substring(1);
        int codeLength = countryCodeLength(digits);
        String countryCode = digits.substring(0, codeLength);
        String national = digits.substring(codeLength);
        return "+" + countryCode + (national.isEmpty() ? "" : " " + groupInThrees(national));
    }

    /** Length of the recognised country code at the start of {@code digits}, else 1. */
    private static int countryCodeLength(String digits) {
        for (Map.Entry<String, Integer> code : COUNTRY_CODE_LENGTHS.entrySet()) {
            if (digits.startsWith(code.getKey())) {
                return code.getValue();
            }
        }
        return 1;
    }

    /** Split the national digits into space-separated groups of three, left to right. */
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
}
