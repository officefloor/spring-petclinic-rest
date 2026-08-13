package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived key that all owner duplicate detection is expressed through:
 * {@code <normalizedTelephone>|<email>|<householdId>}. The telephone is the normalized E.164 form,
 * the email is the lower-cased address (empty when absent) and the householdId is the shared
 * household identifier (empty when the owner belongs to no household). Two owners are duplicates
 * only when their whole identityKey is equal — because the telephone is part of the key, members of
 * the same household with different telephones have different keys and are both allowed.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identity key of a stored owner, from its telephone, email and householdId. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key for the given normalized telephone, email and householdId. */
    public static String of(String telephone, String email, String householdId) {
        return safe(telephone) + "|" + safe(email) + "|" + safe(householdId);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
