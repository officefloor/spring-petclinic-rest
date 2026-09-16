package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The single definition of a SHA-256 fingerprint of a string. Shared by everything that
 * fingerprints owner data — the member id hash and household id (a truncated upper-case
 * prefix) and the identity key (the full lower-case digest) — so they all hash identically.
 */
public final class Sha256 {

    private Sha256() {
    }

    /**
     * The first {@code length} upper-case hex characters of SHA-256 over the UTF-8 bytes of
     * {@code value}.
     *
     * @param value  the string to hash
     * @param length the number of leading hex characters to return
     * @return the truncated upper-case hex digest
     */
    public static String upperHex(String value, int length) {
        return hex(value, "%02X").substring(0, length);
    }

    /**
     * The full 64-character lower-case hex SHA-256 over the UTF-8 bytes of {@code value}.
     *
     * @param value the string to hash
     * @return the complete lower-case hex digest
     */
    public static String lowerHex(String value) {
        return hex(value, "%02x");
    }

    private static String hex(String value, String byteFormat) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format(byteFormat, b & 0xFF));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required to derive a fingerprint", ex);
        }
    }
}
