package org.springframework.samples.petclinic.util;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} - the single value all duplicate detection is expressed
 * through. It is {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)},
 * where the telephone is the owner's stored E.164 form and the email is its stored lower-cased form.
 * Two owners are duplicates when, and only when, their whole identity keys are equal; because the
 * telephone is part of the key, household members with different telephones have different keys.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * @param owner the owner whose identity key to derive.
     * @return the identity key {@code telephone|email|householdId}, with a missing email or
     *         householdId contributing an empty segment.
     */
    public static String key(Owner owner) {
        return segment(owner.getTelephone()) + "|" + segment(owner.getEmail()) + "|"
                + segment(owner.getHouseholdId());
    }

    private static String segment(String value) {
        return value == null ? "" : value;
    }
}
