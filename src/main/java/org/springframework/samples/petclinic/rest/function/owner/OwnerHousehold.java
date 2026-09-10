package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Shared derivation of an owner's household identity. The {@code householdId} is now
 * <em>deterministic</em>: the first 12 hex characters of SHA-256 over
 * {@code normalizedLastName + "|" + postcode}. Owners with the same last name (compared
 * case-insensitively with collapsed whitespace) and the same postcode therefore resolve to
 * the same householdId automatically, without any creation-order back-fill.
 *
 * <p>Used by {@link AssignOwnerHousehold} (which stamps the id onto a new owner), by
 * {@link EnsureUniqueOwnerIdentity} (which blocks a second owner of an existing household) and
 * by {@link AssignOwnerPossibleDuplicate} (which treats fellow household members as declared,
 * not suspected, duplicates), so all three agree on exactly what a household is.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * The deterministic {@code householdId} for the given last name and postcode, or
     * {@code null} when no postcode is supplied (an owner with no postcode has no household).
     */
    static String idFor(String lastName, String postcode) {
        if (postcode == null || postcode.isBlank()) {
            return null;
        }
        return id(normalizeName(lastName), postcode.trim());
    }

    /** Stable 12-char upper-hex identifier derived from the deterministic household key. */
    static String id(String normalizedLastName, String postcode) {
        String key = normalizedLastName + "|" + postcode;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(12);
            for (int i = 0; i < 6; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
