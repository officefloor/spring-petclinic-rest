package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's duplicate-detection identity key, shared by the
 * owner-create pipeline. The key consolidates the former separate telephone, email and
 * household duplicate checks into one derived value so two owners conflict only when their
 * whole keys are equal.
 *
 * <p>The key is {@code normalizedTelephone + "|" + (email or "") + "|" + (householdId or "")}.
 * Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones derive different keys and are both allowed;
 * only an exact full-key match is a duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Build the identity key from already-normalized parts: a normalized E.164 telephone, a
     * lower-cased email (or {@code null}) and a household id (or {@code null}). A {@code null}
     * segment is rendered as empty.
     */
    static String key(String normalizedTelephone, String email, String householdId) {
        return orEmpty(normalizedTelephone) + "|" + orEmpty(email) + "|" + orEmpty(householdId);
    }

    /**
     * The identity key of an owner, re-deriving the normalized telephone and email from the
     * stored values so it compares identically to a freshly built key and can be derived at the
     * Owner-to-DTO boundary for any response.
     */
    public static String of(Owner owner) {
        return key(OwnerTelephone.normalize(owner.getTelephone()),
                OwnerEmail.normalize(owner.getEmail()), owner.getHouseholdId());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
