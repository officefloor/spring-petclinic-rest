package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} and uses it as the single source of truth for
 * duplicate detection: the SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 *
 * <p>Two owners are duplicates only when their WHOLE identityKey matches; because the
 * telephone is part of the key, owners who share a last name and postcode but carry
 * different telephones have different keys and are both allowed (a soft match, not a 409).
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The owner's derived identity key. The telephone is already stored normalised
     *  (E.164); a missing email contributes an empty segment. */
    public static String of(Owner owner) {
        String email = owner.getEmail() == null ? "" : owner.getEmail().toLowerCase();
        return sha256hex("V2|" + owner.getTelephone() + "|" + email + "|" + Soundex.of(owner.getLastName()));
    }

    /** Whether an existing owner shares the candidate's whole identity key. */
    public static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String key = of(candidate);
        return existing.stream().filter(o -> !o.isDeleted()).anyMatch(o -> key.equals(of(o)));
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code s}. */
    private static String sha256hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
