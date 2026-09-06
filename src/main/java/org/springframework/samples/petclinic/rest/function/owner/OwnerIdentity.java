package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single {@code identityKey} that every owner duplicate check is expressed through.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two
 * owners are duplicates only when their WHOLE keys are equal: because the normalized telephone is
 * part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different keys and are both allowed. The telephone is already normalized to E.164
 * and the email to lower case by {@link BuildOwner}; the {@code householdId} is assigned by
 * {@link AssignHousehold}. A missing email or household id contributes an empty segment.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        return segment(owner.getTelephone()) + '|' + segment(owner.getEmail()) + '|'
                + segment(owner.getHouseholdId());
    }

    private static String segment(String value) {
        return value == null || value.isBlank() ? "" : value;
    }
}
