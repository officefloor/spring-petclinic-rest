package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Single source of truth for an owner's {@code identityKey}, the derived value all duplicate
 * detection is expressed through: the SHA-256 hex of {@code normalizedTelephone + '|' + lowerEmail +
 * '|' + soundex(lastName)}. Two owners are duplicates only when their whole identityKey is equal, so
 * members of the same household (same {@link Soundex} last name and postcode) with different
 * telephones have distinct keys and are a soft match, not a hard duplicate.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        return sha256Hex(orEmpty(owner.getTelephone()) + "|" + orEmpty(owner.getEmail()) + "|"
                + Soundex.encode(owner.getLastName()));
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
