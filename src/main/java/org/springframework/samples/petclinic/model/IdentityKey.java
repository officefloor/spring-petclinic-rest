package org.springframework.samples.petclinic.model;

/**
 * Derives an owner's {@code identityKey}: the single value all duplicate detection is
 * expressed through.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}.
 * By the time an owner is built its telephone has been normalized to E.164 and its email
 * lower-cased, so those stored values are the normalized forms. A null email or a null
 * {@code householdId} contributes an empty segment.
 *
 * <p>Two owners are duplicates only when their <em>whole</em> keys are equal. Because the
 * telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
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
