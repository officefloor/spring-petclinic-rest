package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 rendered as lower-case hexadecimal. The full digest is 64 hex characters; callers that
 * want a shorter, upper-cased form (as the deterministic {@link HouseholdId} does) take a prefix and
 * upper-case it themselves. Keeping the primitive in one place lets the several derived identifiers in
 * this package share exactly one hashing implementation.
 */
final class Sha256Hex {

    private Sha256Hex() {
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value} (64 characters). */
    static String of(String value) {
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
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
