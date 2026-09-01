package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Identity rule for owners: every owner has one derived {@code identityKey} that
 * combines its normalized telephone, email and household. A new owner duplicates an
 * existing one only when their WHOLE identityKey is equal, so two members of the same
 * household with different telephones (different keys) are both allowed.
 */
final class Identities {

    private Identities() {
    }

    /** The derived identity key: normalizedTelephone + '|' + (email or empty) + '|' + householdId. */
    static String identityKey(Owner owner) {
        return normalize(owner.getTelephone()) + "|" + normalize(owner.getEmail())
            + "|" + normalize(owner.getHouseholdId());
    }

    /** Whether {@code candidate}'s whole identityKey already belongs to an existing owner. */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String key = identityKey(candidate);
        return existing.stream().anyMatch(other -> key.equals(identityKey(other)));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
