package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes the SHA-256 digest of a string as upper-case hexadecimal. Shared by the derived
 * identities that hash owner attributes (the customer code and the household id).
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The full upper-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02X", b));
        }
        return hex.toString();
    }
}
