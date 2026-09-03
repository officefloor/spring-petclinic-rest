package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's identity key — the single value all duplicate detection is based on.
 * It is the normalized telephone, the email (already lower-cased, empty when absent) and the
 * {@link HouseholdId}, joined by '|'. Two owners are duplicates only when their whole keys
 * match; a shared telephone, email or household alone is not enough.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        return telephone + "|" + email + "|" + HouseholdId.of(owner);
    }
}
