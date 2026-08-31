package org.springframework.samples.petclinic.util;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} and uses it as the single source of truth for
 * duplicate detection: {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
 *
 * <p>Two owners are duplicates only when their WHOLE identityKey matches; because the
 * telephone is part of the key, members of the same household with different telephones
 * carry different keys and are both allowed.
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The owner's derived identity key. The telephone is already stored normalised
     *  (E.164); a missing email or household id contributes an empty segment. */
    public static String of(Owner owner) {
        String email = owner.getEmail() == null ? "" : owner.getEmail();
        String householdId = owner.getHouseholdId() == null ? "" : owner.getHouseholdId();
        return owner.getTelephone() + "|" + email + "|" + householdId;
    }

    /** Whether an existing owner shares the candidate's whole identity key. */
    public static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String key = of(candidate);
        return existing.stream().anyMatch(o -> key.equals(of(o)));
    }
}
