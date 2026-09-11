package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Derives the stable {@code householdId} shared by owners with the same last name and
 * postcode. The id is the {@link #hash(String) SHA-256 prefix} of the normalized last name
 * and the postcode, so every owner in the same household (same normalized last name +
 * postcode) maps to the same value automatically — no opt-in is required.
 *
 * <p>Centralising the derivation here keeps {@link AssignHousehold} (which assigns the id)
 * and {@link IdentityKeys} (which folds it into an owner's identity key) in exact agreement.
 */
public final class Households {

    private Households() {
    }

    /** The stable {@code householdId} for the given last name and postcode. */
    public static String idFor(String lastName, String postcode) {
        return hash(normalize(lastName) + "|" + (postcode == null ? "" : postcode));
    }

    /** Lower-case, trim, and collapse runs of whitespace to a single space. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** Stable 12-hex-character (upper-case) prefix of the SHA-256 of the household key. */
    private static String hash(String value) {
        return Digests.sha256Hex(value).substring(0, 12).toUpperCase(Locale.ROOT);
    }
}
