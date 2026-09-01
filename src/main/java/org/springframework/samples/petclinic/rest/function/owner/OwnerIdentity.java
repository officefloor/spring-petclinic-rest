package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey}, the derived value all duplicate
 * detection is expressed through: {@code normalizedTelephone + '|' + (email or empty) + '|' +
 * householdId}. Two owners are duplicates only when their whole identityKey is equal, so members of
 * the same household (same householdId) with different telephones have distinct keys.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        return orEmpty(owner.getTelephone()) + "|" + orEmpty(owner.getEmail()) + "|"
                + orEmpty(owner.getHouseholdId());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
