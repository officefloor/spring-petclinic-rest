package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value duplicate detection is expressed
 * through. It is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is E.164 (see {@link E164Telephone}), the email is lower-cased (empty when absent) and
 * the household id groups owners by last name and address (see {@link HouseholdId}). Because the
 * telephone is part of the key, two members of one household with different telephones have
 * different keys and are both allowed; only owners that collide on the key are duplicates.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        return telephone(owner) + '|' + email(owner) + '|' + HouseholdId.of(owner);
    }

    static String telephone(Owner owner) {
        String telephone = E164Telephone.toE164(owner.getTelephone());
        return telephone == null ? "" : telephone;
    }

    static String email(Owner owner) {
        String email = owner.getEmail();
        return (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
    }
}
