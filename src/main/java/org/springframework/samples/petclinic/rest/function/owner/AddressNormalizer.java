package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Canonicalises a raw address string: trim and collapse internal whitespace runs to a
 * single space, upper-case, and expand common street-type abbreviations
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The result is the
 * form stored and returned as {@code address}, and the form every address comparison
 * (household duplicate detection and the shared household id) is done against.
 * Normalization is idempotent, so re-normalizing an already-normalized address is a
 * no-op. Returns the empty string when no address text remains, so callers reject a
 * blank-after-normalization address with a 400.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(expand(tokens[i]));
        }
        return result.toString();
    }

    /** Expand a single upper-cased token if it is a known street-type abbreviation. */
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
