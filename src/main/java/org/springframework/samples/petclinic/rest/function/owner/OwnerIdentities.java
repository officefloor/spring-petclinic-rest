package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.samples.petclinic.util.Soundex;

/**
 * Shared owner identity for the create-owner pipeline: derives the single canonical
 * {@code identityKey} that consolidates all duplicate detection.
 *
 * <p>The key is the lower-case hex SHA-256 (see {@link Sha256}) of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, with telephone
 * canonicalized to E.164 (see {@link OwnerTelephones}), email lower-cased (see
 * {@link OwnerEmails}) and the last name reduced to its Soundex code (see {@link Soundex}); a
 * missing telephone or email contributes an empty string. Two owners share this key only when
 * they submit the same telephone, email and phonetically-equal surname — {@link RequireUniqueIdentity}
 * uses it to reject an exact resubmission.
 */
public final class OwnerIdentities {

    private OwnerIdentities() {
    }

    /**
     * Canonical identity key for the given components: the SHA-256 hex of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. {@code telephone}
     * is normalized to E.164 and {@code email} lower-cased before assembly; a {@code null}
     * telephone or email contributes an empty string.
     */
    public static String key(String telephone, String email, String lastName) {
        String canonical = part(OwnerTelephones.toE164(telephone)) + "|"
                + part(OwnerEmails.normalize(email)) + "|" + Soundex.encode(lastName);
        return Sha256.hex(canonical);
    }

    /** Canonical {@link #key(String, String, String)} for an existing owner's stored fields. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String part(String value) {
        return value == null ? "" : value;
    }
}
