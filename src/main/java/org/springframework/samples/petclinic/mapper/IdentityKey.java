package org.springframework.samples.petclinic.mapper;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single duplicate-detection key that all owner
 * duplicate rules are now expressed through. The key is
 * {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}, where the
 * telephone is the stored (already E.164-normalized) value, the email is lower-cased or an
 * empty segment when absent, and the householdId is an empty segment when the owner is not
 * in a shared household.
 *
 * <p>Two owners are duplicates only when their <em>whole</em> identityKey is equal: because
 * the telephone is part of the key, two members of the same household (same householdId)
 * with different telephones have different keys and are both allowed. Kept out of
 * {@link OwnerMapper} so MapStruct does not mistake the helper for an implicit mapping method.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The duplicate-detection identity key for {@code owner}. */
    public static String of(Owner owner) {
        return segment(owner.getTelephone()) + "|" + email(owner.getEmail()) + "|"
                + segment(owner.getHouseholdId());
    }

    private static String segment(String value) {
        return value == null ? "" : value;
    }

    private static String email(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
