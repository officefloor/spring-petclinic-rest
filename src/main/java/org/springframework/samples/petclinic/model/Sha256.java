package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single source of truth for SHA-256 hashing used across owner identity derivations. The one place the
 * {@code MessageDigest} lookup, UTF-8 encoding, byte-to-hex conversion and {@code NoSuchAlgorithmException}
 * handling live, so every hashed identity (the {@link Owner#identityKey identity key}, the household id and
 * the customer-code hash) composes the same digest the same way and cannot drift. Callers take the prefix,
 * case or length they need from the full lower-case hex returned by {@link #hex(String)}.
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The lower-case SHA-256 hex digest (64 characters) of the UTF-8 bytes of {@code value}. */
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
