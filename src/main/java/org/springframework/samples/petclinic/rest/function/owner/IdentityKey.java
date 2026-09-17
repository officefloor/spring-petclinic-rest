package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived duplicate-detection key for owners: the canonical telephone, the canonical email
 * (empty when absent) and the household id (empty when the owner is in no shared household), joined by
 * '|'. {@link EnsureUniqueIdentity} rejects a create request whose whole key equals an existing
 * owner's, and {@link org.springframework.samples.petclinic.mapper.OwnerMapper} returns it on the
 * response. Consolidating telephone, email and household into one key means two owners collide only on
 * an exact full-key match: sharing a household (same household id) but differing in telephone yields
 * different keys and is allowed. Not a pipeline step, so it exposes plain helpers.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key of an existing or freshly built owner. */
    public static String forOwner(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /**
     * Builds the identity key from its parts: the telephone in canonical E.164 form (see
     * {@link TelephoneNormalizer}), the email lower-cased or empty when absent or blank (see
     * {@link EmailNormalizer}) and the household id or empty when absent, separated by '|'.
     */
    public static String of(String telephone, String email, String householdId) {
        return normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|" + orEmpty(householdId);
    }

    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneNormalizer.toE164(telephone);
        return e164 == null ? orEmpty(telephone) : e164;
    }

    private static String normalizeEmail(String email) {
        return (email == null || email.isBlank()) ? "" : EmailNormalizer.normalize(email);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
