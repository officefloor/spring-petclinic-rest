package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.rest.function.common.IdentityVersion;
import org.springframework.samples.petclinic.rest.function.common.Sha256;

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

    /**
     * Stable 12-char upper-hex identifier derived from the deterministic household key. Version 2
     * mixes the fixed 'V2' tag into the hash input so no version-1 householdId recurs, while owners
     * of the same household still resolve to the same identifier.
     */
    static String id(String normalizedLastName, String postcode) {
        return Sha256.hexPrefix(IdentityVersion.TAG + "|" + normalizedLastName + "|" + postcode, 12);
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimming the ends. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
