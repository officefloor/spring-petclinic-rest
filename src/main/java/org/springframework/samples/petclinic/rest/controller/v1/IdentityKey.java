package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of duplicate detection: an owner's identity is its normalized telephone,
 * email and household id joined as {@code normalizedTelephone|email|householdId} (email and
 * household id empty when absent). Two owners collide only when their whole keys are equal, so
 * members of one household with different telephones stay distinct.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The derived identity key of {@code owner}; telephone is already E.164-normalized upstream. */
    public static String of(Owner owner) {
        return blank(owner.getTelephone()) + "|" + blank(owner.getEmail()) + "|" + blank(owner.getHouseholdId());
    }

    /** {@code true} when an existing owner has the exact same identity key as {@code candidate}. */
    static boolean isDuplicate(Owner candidate, Collection<Owner> existing) {
        String key = of(candidate);
        return existing.stream().anyMatch(other -> of(other).equals(key));
    }

    private static String blank(String value) {
        return value == null ? "" : value;
    }
}
