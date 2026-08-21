package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is now
 * expressed through:
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}.
 *
 * <p>The components are taken from the owner as already canonicalised earlier in the create
 * pipeline: the telephone in E.164 form (see {@link TelephoneE164}), the email lower-cased,
 * and the shared {@code householdId} once {@link AssignHousehold} has assigned it. Because the
 * telephone is part of the key, two members of the same household (same {@code householdId})
 * with different telephones have different identity keys; only an exact whole-key match is a
 * duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = (owner.getEmail() == null || owner.getEmail().isBlank()) ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return telephone + "|" + email + "|" + householdId;
    }
}
