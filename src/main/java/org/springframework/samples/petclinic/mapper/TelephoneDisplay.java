package org.springframework.samples.petclinic.mapper;

import java.util.List;

/**
 * Formats a stored E.164 telephone number for human display: the country code, a
 * single space, then the national digits grouped in threes from the left (for
 * example {@code "+61412345678"} becomes {@code "+61 412 345 678"}). The raw E.164
 * value stays the canonical stored form; this is a read-only presentation derived
 * from it.
 *
 * <p>Kept as a plain static helper - rather than a method on {@link OwnerMapper} -
 * so MapStruct does not mistake it for an implicit String-to-String property
 * mapping and apply it to unrelated fields.
 */
public final class TelephoneDisplay {

    /**
     * Known country calling codes (including the leading {@code '+'}), longest first
     * so the longest matching prefix wins. A stored number beginning with none of
     * these falls back to a single-digit country code.
     */
    private static final List<String> COUNTRY_CODES = List.of("+61", "+1");

    private TelephoneDisplay() {
    }

    /**
     * Formats the given stored E.164 telephone number for display. The country code
     * is separated from the national digits by a single space, and the national
     * digits are grouped in threes from the left.
     *
     * @param telephone the stored E.164 telephone number (a {@code '+'} followed by
     *                   digits), or {@code null}
     * @return the human-formatted number, or the input unchanged when it is
     *         {@code null} or not in E.164 form
     */
    public static String forE164(String telephone) {
        if (telephone == null || !telephone.startsWith("+")) {
            return telephone;
        }
        String countryCode = countryCodeOf(telephone);
        String grouped = groupInThrees(telephone.substring(countryCode.length()));
        return grouped.isEmpty() ? countryCode : countryCode + " " + grouped;
    }

    private static String countryCodeOf(String telephone) {
        for (String code : COUNTRY_CODES) {
            if (telephone.startsWith(code)) {
                return code;
            }
        }
        return telephone.substring(0, Math.min(2, telephone.length()));
    }

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
