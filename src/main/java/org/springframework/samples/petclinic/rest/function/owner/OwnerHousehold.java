package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of household membership, used by {@link AssignOwnerHousehold} (which
 * assigns the shared householdId), so the assignment rule lives in one place.
 * Two owners are in the same household when their lastName and address match. lastName is
 * compared case-insensitively with collapsed whitespace; address is compared using the
 * shared {@link AddressNormalizer} form, so the guard and the assignment agree with the
 * address that is stored and returned.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /** Whether an owner with the given lastName and address shares a household with {@code other}. */
    static boolean isSameHousehold(String lastName, String address, Owner other) {
        return normalize(lastName).equals(normalize(other.getLastName()))
                && AddressNormalizer.normalize(address).equals(AddressNormalizer.normalize(other.getAddress()));
    }

    /** Lower-case and collapse whitespace runs to a single space for comparison. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
