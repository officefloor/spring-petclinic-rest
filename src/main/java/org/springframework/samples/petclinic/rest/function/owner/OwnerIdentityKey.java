package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The read-only {@code identityKey} shown on the owner response:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. That string embeds the
 * deterministic {@link HouseholdId}; it is descriptive only and is not itself the basis of the
 * duplicate check ({@link EnsureUniqueIdentity}).
 *
 * <ul>
 * <li><b>normalizedTelephone</b> — the E.164 form (see {@link E164Telephone}); the stored telephone
 * is already E.164 and re-normalizing is idempotent.</li>
 * <li><b>email</b> — the lower-cased address, or empty when the owner has none.</li>
 * <li><b>householdId</b> — the owner's deterministic {@link HouseholdId}.</li>
 * </ul>
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The descriptive identity key of an already-built/stored owner, for the response. */
    public static String of(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** The identity key from raw parts. Null telephone or email contribute an empty segment. */
    static String build(String telephone, String email, String householdId) {
        String tel = E164Telephone.toE164OrNull(telephone);
        return (tel == null ? "" : tel) + "|"
                + (email == null ? "" : email.trim().toLowerCase()) + "|"
                + (householdId == null ? "" : householdId);
    }
}
