package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.util.Sha256;

/**
 * Canonical household comparison shared by the create pipeline: {@link AssignHousehold} uses it to
 * decide whether two owners share a household and to derive the shared household id (which in turn
 * feeds the household part of the {@link IdentityKey duplicate key}). A household is identified by last
 * name plus address: the last name is compared
 * case-insensitively with runs of whitespace collapsed to a single space and outer whitespace trimmed,
 * and the address is compared in its canonical form (see {@link AddressNormalizer}). Not a pipeline
 * step, so it is free to expose plain helpers.
 */
public final class HouseholdNormalizer {

    private HouseholdNormalizer() {
    }

    /**
     * The stable household identifier derived from an owner's last name and address. Owners with
     * equal {@link #key(String, String) keys} share the same id, and the id never changes for a given
     * household, so it can be recomputed for late joiners rather than stored and looked up.
     */
    public static String id(String lastName, String address) {
        return "H-" + Sha256.prefix(key(lastName, address), 12);
    }

    /**
     * Builds the canonical household key for an owner from its last name and address. Two owners
     * belong to the same household when their keys are equal. Either field may be {@code null}, which
     * normalizes to an empty token.
     */
    public static String key(String lastName, String address) {
        // '\n' separates the fields so that a boundary shift (e.g. "ab"+"c" vs "a"+"bc") cannot forge a
        // collision; it never appears in a normalized value.
        return normalizeName(lastName) + "\n" + AddressNormalizer.normalize(address);
    }

    /** Lower-cases and collapses whitespace to single spaces, trimming the ends; {@code null} → "". */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
