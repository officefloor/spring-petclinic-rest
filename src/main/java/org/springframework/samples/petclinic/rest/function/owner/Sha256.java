package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hashing to a hexadecimal string — the single place the owner functions compute a digest,
 * so everything that derives a value from a hash encodes it the same way. {@link Household#id(String,
 * String)} and {@link AssignCustomerCode} both hash through here; a caller that wants a short,
 * upper-case code takes a prefix of {@link #hex(String)} and upper-cases it.
 */
final class Sha256 {

    private Sha256() {
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code input} (64 characters). */
    static String hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
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
