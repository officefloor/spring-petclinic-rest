package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 hex digest of a string's UTF-8 bytes. Shared by the deterministic identifiers the
 * owner pipeline derives (the household id and the customer code hash), so they all agree on
 * one digest representation.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
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
