package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Normalizes a postal address to a single canonical form: trimmed with runs of whitespace
 * collapsed to a single space, upper-cased, and common street-type abbreviations expanded
 * to their full word (ST -> STREET, RD -> ROAD, AVE -> AVENUE). Expansion matches whole
 * space-separated tokens only, so "12 MAIN ST" becomes "12 MAIN STREET" while a token such
 * as "TEST" is left untouched.
 *
 * <p>Every create-owner comparison of addresses (household duplicate detection and the
 * shared household id) runs through this so that inputs like "12 main st" and
 * "12 MAIN STREET" are treated as the same address.
 *
 * <p>A plain utility (not an OfficeFloor function), so it may expose helpers without
 * tripping the one-public-method-per-function rule.
 */
public final class AddressNormalizer {

    private AddressNormalizer() {
    }

    /**
     * @return the canonical address form; {@code ""} when the input is {@code null} or blank.
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = expand(tokens[i]);
        }
        return String.join(" ", tokens);
    }

    private static String expand(String token) {
        switch (token) {
            case "ST":
                return "STREET";
            case "RD":
                return "ROAD";
            case "AVE":
                return "AVENUE";
            default:
                return token;
        }
    }
}
