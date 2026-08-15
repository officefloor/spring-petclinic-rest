package org.springframework.samples.petclinic.rest.function.common;

import java.util.List;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats the stored E.164 telephone for humans for the read-only {@code telephoneDisplay} field:
 * the country code, a space, then the national digits grouped in threes from the left
 * (e.g. {@code +61412345678} -> {@code +61 412 345 678}). The raw {@code telephone} stays E.164.
 */
public final class Telephones {

    /**
     * Known country codes, longest first so a longer code is matched before a shorter prefix of it.
     * Mirrors the codes recognised when normalising to E.164 in {@code ValidateOwnerFields}.
     */
    private static final List<String> COUNTRY_CODES = List.of("61", "1");

    private Telephones() {
    }

    /** The owner's stored telephone formatted for display, or {@code null} when none is on record. */
    public static String displayOf(Owner owner) {
        return displayOf(owner.getTelephone());
    }

    /**
     * Format an E.164 telephone as {@code +<countryCode> <national digits grouped in threes>}.
     * A value that is not in E.164 form (null, or not a {@code +} followed by digits) is returned
     * unchanged, since it cannot be split into a country code and national number.
     */
    public static String displayOf(String telephone) {
        if (telephone == null || !telephone.matches("\\+[0-9]+")) {
            return telephone;
        }
        String digits = telephone.substring(1);
        String code = COUNTRY_CODES.stream().filter(digits::startsWith).findFirst().orElse("");
        String national = digits.substring(code.length());
        return "+" + code + " " + groupInThrees(national);
    }

    /** Group the digits into space-separated runs of three from the left (last run may be shorter). */
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
