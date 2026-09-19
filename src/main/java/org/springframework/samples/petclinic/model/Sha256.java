package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes the SHA-256 digest of a string as hexadecimal. Shared by the derived identities that
 * hash owner attributes: the member id and household id use the upper-case {@link #hex} form,
 * the identity key the lower-case {@link #lowerHex} form.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The full upper-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        return digestHex(value, "%02X");
    }

    /** The full lower-case hex SHA-256 of the UTF-8 bytes of {@code value} (64 characters). */
    public static String lowerHex(String value) {
        return digestHex(value, "%02x");
    }

    private static String digestHex(String value, String byteFormat) {
        byte[] digest;
        try {
            digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format(byteFormat, b));
        }
        return hex.toString();
    }
}
