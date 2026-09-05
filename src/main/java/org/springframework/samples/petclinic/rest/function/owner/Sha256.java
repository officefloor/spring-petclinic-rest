package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 of a string to its lower-case hex form — the one place the owner pipeline turns a value
 * into a digest. The derived keys differ only in how much of it they keep: {@link HouseholdId} and
 * {@link AssignCustomerCode} take a fixed-width, upper-case leading slice, while a full-width key
 * uses all 64 characters. Centralising the digest-and-hex step keeps every derived key hashing the
 * same way.
 */
final class Sha256 {

    private Sha256() {
    }

    /** The full lower-case hex SHA-256 (64 characters) of the UTF-8 bytes of {@code value}. */
    static String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
