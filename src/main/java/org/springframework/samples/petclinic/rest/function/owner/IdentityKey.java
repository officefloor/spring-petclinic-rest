package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} = normalized telephone + '|' + (lower-cased email or
 * empty) + '|' + household id. Duplicate detection is a single equality on this whole key, and it
 * is also returned on the owner. Two owners collide only when telephone, email and household all
 * match; sharing only a household (with different telephones) yields different keys.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        String email = owner.getEmail() == null || owner.getEmail().isBlank()
                ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        return owner.getTelephone() + "|" + email + "|" + owner.getHouseholdId();
    }
}
