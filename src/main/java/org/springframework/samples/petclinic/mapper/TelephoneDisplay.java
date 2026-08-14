package org.springframework.samples.petclinic.mapper;

import java.util.Comparator;
import java.util.Set;

/**
 * Formats a stored E.164 telephone number for human display.
 *
 * <p>The E.164 value ('+' followed by country code and national digits) is rendered as the country
 * code, a single space, then the national digits grouped in threes from the left, e.g.
 * {@code '+61412345678'} becomes {@code '+61 412 345 678'}. The raw {@code telephone} is left in
 * E.164 form; this is a read-only display companion.
 *
 * <p>Kept as a standalone helper rather than a method on {@link OwnerMapper}: a single-argument
 * {@code String}-to-{@code String} method declared on a MapStruct mapper would be picked up as an
 * implicit conversion and applied to every String property mapping.
 */
public final class TelephoneDisplay {

    /**
     * Known E.164 country codes, matched longest-first so {@code '61'} wins over {@code '1'}. A
     * number that starts with no known code is grouped as a whole after the {@code '+'}.
     */
    private static final Set<String> COUNTRY_CODES = Set.of("1", "61");

    private TelephoneDisplay() {
    }

    /**
     * Formats {@code telephone} (an E.164 string such as {@code '+61412345678'}) as
     * {@code '+<countryCode> <national digits grouped in threes>'}. A {@code null} or blank value,
     * or one carrying no digits, is returned unchanged.
     */
    public static String of(String telephone) {
        if (telephone == null || telephone.isBlank()) {
            return telephone;
        }
        String digits = telephone.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return telephone;
        }
        String countryCode = COUNTRY_CODES.stream()
            .filter(digits::startsWith)
            .max(Comparator.comparingInt(String::length))
            .orElse("");
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        boolean needsSpace = !countryCode.isEmpty();
        for (int i = 0; i < national.length(); i += 3) {
            if (needsSpace) {
                sb.append(' ');
            }
            needsSpace = true;
            sb.append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }
}
