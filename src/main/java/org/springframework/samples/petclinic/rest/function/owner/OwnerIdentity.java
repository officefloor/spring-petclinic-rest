package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's identity key for the informational {@code identityKey} response field,
 * computed once at the Owner-to-DTO boundary ({@code OwnerMapper}). The key is
 * {@code normalizedTelephone + "|" + (email or "") + "|" + (householdId or "")}.
 *
 * <p>It no longer gates duplicate detection. A create is now rejected as a <em>household</em>
 * duplicate when an existing owner already carries the household id derived from
 * {@code (lastName, postcode)} — see {@link ValidateUniqueIdentity} — so this key survives only
 * to populate the response field for callers.
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
