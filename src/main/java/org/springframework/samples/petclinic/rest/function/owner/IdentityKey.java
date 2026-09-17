package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * A derived, opaque token summarising an owner's contact identity: the SHA-256 hex of the canonical
 * telephone, the canonical email (empty when absent) and the {@link Soundex} of the last name, joined
 * by '|'. {@link org.springframework.samples.petclinic.mapper.OwnerMapper} returns it on the response
 * so a client can compare owners at a glance, and the create pipeline's duplicate detection keys off
 * it directly (see {@link EnsureUniqueIdentity}). Not a pipeline step, so it exposes plain helpers.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key of an existing or freshly built owner. */
    public static String forOwner(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * Builds the identity key from its parts: the SHA-256 hex over the telephone in canonical E.164
     * form (see {@link TelephoneNormalizer}), the email lower-cased or empty when absent or blank (see
     * {@link EmailNormalizer}) and the {@link Soundex} of the last name, separated by '|'.
     */
    public static String of(String telephone, String email, String lastName) {
        return Sha256.hex(normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|" + Soundex.of(lastName));
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
