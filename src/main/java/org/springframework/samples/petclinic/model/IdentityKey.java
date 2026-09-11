package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code identityKey}, a descriptive value exposed on the owner
 * response.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
 * By the time an owner is built its telephone has been normalized to E.164 and its email
 * lower-cased, so those stored values are the normalized forms. A null email or a null
 * {@code householdId} contributes an empty segment.
 *
 * <p>Duplicate detection itself is now keyed on the {@code householdId} alone (see the
 * create pipeline's household duplicate block), not on this whole key.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key for the given owner. */
    public static String of(Owner owner) {
        return segment(owner.getTelephone()) + "|" + segment(owner.getEmail()) + "|"
                + segment(owner.getHouseholdId());
    }

    private static String segment(String value) {
        return value == null ? "" : value;
    }
}
