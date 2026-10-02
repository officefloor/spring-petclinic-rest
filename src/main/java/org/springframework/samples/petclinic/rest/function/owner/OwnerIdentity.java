package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Builds an owner's {@code identityKey} — the one derived value all duplicate detection is expressed
 * through — from possibly non-canonical fields, so the create-time check can compare a request against
 * stored owners on equal terms. The key is
 * {@code normalizedTelephone + "|" + (email or empty) + "|" + householdId}; its canonical {@code "|"}
 * format lives in exactly one place, {@link Owner#identityKey(String, String, String)}, which this
 * method delegates to after normalizing the telephone. There is no second format here.
 *
 * <p>Normalization: the telephone is coerced to E.164 (see {@link OwnerTelephone}; an invalid value
 * contributes an empty telephone component — validation happens elsewhere) and the email is lower-cased
 * by the delegate. Because the normalized telephone is part of the key, two members of the same
 * household with different telephones have different keys and are both allowed; only an exact full-key
 * match is a duplicate.
 *
 * <p>The {@code householdId} is derived and assigned only once an owner is created, so when a key is
 * built for the create-time uniqueness check it is not yet known and the household component is empty
 * for both the candidate and the owners it is compared against (see {@link CheckIdentityUnique}); the
 * key returned on a created owner carries its assigned {@code householdId}.
 */
final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identity key for the given owner fields, with the telephone normalized to E.164 and the email
     * and household id handled by {@link Owner#identityKey(String, String, String)}.
     */
    static String of(String telephone, String email, String householdId) {
        return Owner.identityKey(OwnerTelephone.toE164(telephone), email, householdId);
    }
}
