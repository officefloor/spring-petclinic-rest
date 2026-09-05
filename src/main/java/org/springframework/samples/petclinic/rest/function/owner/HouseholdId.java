package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Derives the stable {@code householdId} shared by owners with the same lastName and
 * address. Names and addresses are compared case-insensitively with collapsed whitespace,
 * matching {@link RequireUniqueHousehold}, so co-resident same-lastName owners always
 * resolve to one identifier.
 */
public final class HouseholdId {

    private HouseholdId() {
    }

    public static String of(String lastName, String address) {
        String key = normalise(lastName) + '|' + normalise(address);
        return "H-" + Integer.toHexString(key.hashCode());
    }

    private static String normalise(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
