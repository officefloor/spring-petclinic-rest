package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Canonical normalization for an owner's postal {@code address}, applied whenever an owner is
 * created. The address is trimmed, its internal whitespace runs are collapsed to a single space,
 * it is upper-cased, and common street-type abbreviations are expanded on a whole-token basis
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
 *
 * <p>The normalized value is what gets stored and returned, and it is the form every comparison of
 * addresses uses — the required-field blank check, household-duplicate detection and the shared
 * household id. Normalization is idempotent, so re-normalizing an already-normalized address is a
 * no-op. A {@code null} or whitespace-only address normalizes to the empty string.
 */
final class AddressNormalizer {

    private AddressNormalizer() {
    }

    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String collapsed = value.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(expand(tokens[i]));
        }
        return sb.toString();
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
