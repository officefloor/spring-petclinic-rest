package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity used for owner duplicate detection.
 *
 * <p>An owner's {@code identityKey} is {@code normalizedTelephone + '|' + (email or empty) + '|' +
 * (householdId or empty)}, computed from the owner's already-normalized stored fields: the E.164
 * telephone (see {@link NormalizeOwnerTelephone}), the lower-cased email (see
 * {@link NormalizeOwnerEmail}), and the {@code householdId} assigned by {@link AssignHousehold}.
 * Two owners are duplicates only when their <em>whole</em> keys are equal — consolidating what were
 * once separate telephone, email and household checks into this one comparison. Because the
 * telephone is part of the key, two members of one household (same {@code householdId}) with
 * different telephones have different keys and are both allowed.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identity key for an owner, from its stored (normalized) telephone, email and householdId. */
    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = (owner.getEmail() == null || owner.getEmail().isBlank()) ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }
}
