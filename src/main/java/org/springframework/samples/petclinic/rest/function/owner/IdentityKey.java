package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * A derived, opaque summary of an owner's contact identity: the canonical telephone, the canonical
 * email (empty when absent) and the household id, joined by '|'.
 * {@link org.springframework.samples.petclinic.mapper.OwnerMapper} returns it on the response so a
 * client can compare owners at a glance. Duplicate detection itself keys off the household id alone
 * (see {@link EnsureUniqueIdentity}); this value simply surfaces the parts. Not a pipeline step, so it
 * exposes plain helpers.
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
