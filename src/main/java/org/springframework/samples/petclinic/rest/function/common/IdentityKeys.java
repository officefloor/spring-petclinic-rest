package org.springframework.samples.petclinic.rest.function.common;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code identityKey}, the single value all create-owner duplicate detection is
 * based on.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)},
 * built from values that are already in their canonical form by the time they reach here — the
 * telephone normalized to E.164 and the email lower-cased. A create is rejected with 409 Conflict
 * only when a new owner's <em>whole</em> key equals an existing owner's; because the telephone is
 * part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different keys and are both allowed.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of a persisted owner, from its stored telephone, email and household id. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key for the given components; a {@code null} component contributes an empty part. */
    public static String of(String telephone, String email, String householdId) {
        return safe(telephone) + "|" + safe(email) + "|" + safe(householdId);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
