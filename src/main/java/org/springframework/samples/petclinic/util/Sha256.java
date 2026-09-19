package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hashing rendered as a lower-case hex string. A single home for the digest so the
 * several identifiers derived from a hash (household id, customer code) share one
 * implementation instead of each re-deriving it.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16));
            hex.append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
