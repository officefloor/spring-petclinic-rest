package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's identity key — the single key that both gates duplicate detection and
 * populates the informational {@code identityKey} response field. The key is the lower-case
 * SHA-256 hex digest of
 * {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName) + "|" + IDENTITY_VERSION}.
 * The {@link Owner#IDENTITY_VERSION version-2 tag} is mixed into the seed, so every identity key
 * differs from the value it carried under version 1 and no version-1 key is produced again.
 *
 * <p>Because the telephone is part of the key, two owners sharing a last name and postcode but
 * carrying different telephones derive different keys: they are no longer a hard duplicate, but a
 * soft match (see {@link PossibleDuplicate}). The duplicate gate ({@link ValidateUniqueIdentity})
 * rejects a create with a 409 only when an existing active owner carries the identical key.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Build the identity key from already-normalized parts: a normalized E.164 telephone, a
     * lower-cased email (or {@code null}) and a last name. A {@code null} telephone or email
     * segment is rendered as empty; the last name is reduced to its {@link Soundex} code.
     */
    static String key(String normalizedTelephone, String email, String lastName) {
        String seed = orEmpty(normalizedTelephone) + "|" + orEmpty(email) + "|" + Soundex.encode(lastName)
                + "|" + Owner.IDENTITY_VERSION;
        return sha256hex(seed);
    }

    /**
     * The identity key for the given raw parts, normalizing the telephone and email first so the
     * result compares identically whether built from a request body or an existing owner.
     */
    static String of(String telephone, String email, String lastName) {
        return key(OwnerTelephone.normalize(telephone), OwnerEmail.normalize(email), lastName);
    }

    /**
     * The identity key of an owner, re-deriving the normalized telephone and email from the stored
     * values so it compares identically to a freshly built key and can be derived at the
     * Owner-to-DTO boundary for any response.
     */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code seed}. */
    private static String sha256hex(String seed) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(seed.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
