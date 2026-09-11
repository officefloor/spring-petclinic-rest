package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Computes an owner's {@code identityKey}: the SHA-256 hex digest over
 * {@code "V2" + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, the
 * fixed {@link IdentityVersion#TAG version-2 tag} folded in first. It is the single
 * identity used for duplicate detection by {@link RequireUniqueIdentity} — two owners collide
 * (409) only when all three components match.
 *
 * <ul>
 * <li>telephone is normalized to E.164 form ({@link Telephones#toE164(String)}), contributing
 *     empty when it cannot form a valid number,
 * <li>email is trimmed and lower-cased, contributing empty when absent,
 * <li>lastName is folded to its {@link Soundex phonetic code}, so households that differ only
 *     by telephone or email produce different keys and are not hard duplicates.
 * </ul>
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of an existing owner, from its stored fields. */
    public static String forOwner(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The identity key for an incoming create request, from its raw fields. Lets
     * {@link RequireUniqueIdentity} compare a request against existing owners'
     * {@link #forOwner(Owner) keys} without first building an entity.
     */
    public static String forFields(String telephone, String email, String lastName) {
        return build(telephone, email, lastName);
    }

    private static String build(String telephone, String email, String lastName) {
        String tel = Telephones.toE164(telephone);
        String raw = IdentityVersion.TAG + "|" + (tel == null ? "" : tel) + "|"
                + normalizeEmail(email) + "|" + Soundex.encode(lastName);
        return Digests.sha256Hex(raw);
    }

    /** Trim and lower-case; null or blank yields empty (no email to contribute). */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
