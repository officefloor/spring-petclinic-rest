package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Canonical household comparison shared by the create pipeline: {@link EnsureUniqueHousehold} uses it
 * to decide whether two owners share a household. A household is identified by last name plus address,
 * each compared case-insensitively with runs of whitespace collapsed to a single space and outer
 * whitespace trimmed. Not a pipeline step, so it is free to expose plain helpers.
 */
public final class HouseholdNormalizer {

    private HouseholdNormalizer() {
    }

    /**
     * Builds the canonical household key for an owner from its last name and address. Two owners
     * belong to the same household when their keys are equal. Either field may be {@code null}, which
     * normalizes to an empty token.
     */
    public static String key(String lastName, String address) {
        // '\n' separates the fields so that a boundary shift (e.g. "ab"+"c" vs "a"+"bc") cannot forge a
        // collision; it never appears in a normalized value.
        return normalize(lastName) + "\n" + normalize(address);
    }

    /** Lower-cases and collapses whitespace to single spaces, trimming the ends; {@code null} → "". */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
