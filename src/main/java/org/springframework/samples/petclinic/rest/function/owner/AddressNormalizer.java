package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Map;

/**
 * Canonical form for an owner address, applied whenever an owner is created. The address is trimmed,
 * its internal whitespace collapsed to single spaces, upper-cased, and common abbreviations expanded
 * on a whole-word basis ({@code ST}&rarr;{@code STREET}, {@code RD}&rarr;{@code ROAD},
 * {@code AVE}&rarr;{@code AVENUE}). The result is idempotent, so normalizing an already-normalized
 * value leaves it unchanged.
 *
 * <p>Used to store/return the address in a single canonical form. (Household grouping is now keyed on
 * last name and postcode, not the address; see {@link OwnerIdentityKey}.)
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
            "ST", "STREET",
            "RD", "ROAD",
            "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /** Normalize an address; {@code null} becomes an empty string. */
    public static String normalize(String value) {
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
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
