package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Lower-case hexadecimal SHA-256 of a string's UTF-8 bytes — the one place the digest is
 * computed for the values several derived fields hang off (an owner's {@code householdId}
 * and the HASH8 segment of its {@code memberId}). Callers that want a shorter code take
 * a prefix of {@link #hex(String)} (and upper-case it where required), keeping the digest
 * itself in a single place rather than re-deriving it per field.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The full 64-character lower-case hex SHA-256 over the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
