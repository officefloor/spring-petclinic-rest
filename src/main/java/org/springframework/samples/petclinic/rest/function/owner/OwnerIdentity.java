package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's derived {@code identityKey} — the descriptive key returned
 * on an owner. The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId},
 * where the telephone is reduced to E.164 through {@link OwnerTelephone#toE164} and the email
 * lower-cased through {@link OwnerEmail#normalize}; a missing email or household id contributes an
 * empty segment.
 *
 * <p>The {@code householdId} segment is the deterministic household id (see
 * {@link OwnerHousehold#householdId}), so two owners in the same household share that segment.
 * This class only derives the key the owner mapper returns.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the identity key from its three raw parts: the submitted/stored telephone (normalized
     * to E.164 here), the email (lower-cased here) and the already-derived household id. Any part
     * that is {@code null} or cannot normalize contributes an empty segment.
     */
    public static String identityKey(String telephone, String email, String householdId) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        String normalizedEmail = OwnerEmail.normalize(email);
        return (normalizedTelephone == null ? "" : normalizedTelephone) + "|"
                + (normalizedEmail == null ? "" : normalizedEmail) + "|"
                + (householdId == null ? "" : householdId);
    }

    /**
     * The identity key of an already-stored owner, read from its persisted fields. The single
     * {@code Owner}-to-key definition used by the owner mapper to return the descriptive key.
     */
    public static String identityKey(Owner owner) {
        return identityKey(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }
}
