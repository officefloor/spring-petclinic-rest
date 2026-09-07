package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Household identity for pet owners. Two owners belong to the same household when they share a last
 * name (compared case-insensitively with collapsed whitespace) and a postcode. The
 * {@link #id(String, String)} value is a stable identifier derived purely from those two fields, so
 * every owner in a household returns the same {@code householdId} without any stored state. Because
 * the household is keyed on those fields alone, two owners sharing last name and postcode are the
 * same household automatically — there is no stored link. Used as one component of the owner
 * {@link OwnerIdentity} key, by {@link RequireUniqueIdentity} to detect a household duplicate,
 * by {@link CountHouseholdMembers} to size the household and by the owner mapper to expose the
 * identifier on responses.
 */
public final class Household {

    private Household() {
    }

    /** Trim, collapse internal whitespace and lower-case for case-insensitive comparison. */
    public static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /**
     * A stable shared identifier for the household of an owner with the given last name and postcode:
     * the first 12 hex characters of the SHA-256 digest over {@code normalizedLastName + '|' +
     * postcode}. Derived purely from those two fields, so owners in the same household get the same
     * value.
     */
    public static String id(String lastName, String postcode) {
        String key = normalize(lastName) + "|" + (postcode == null ? "" : postcode);
        return Sha256.hex(key).substring(0, 12).toUpperCase();
    }
}
