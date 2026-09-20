package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Shared SHA-256 hashing used to derive stable identifiers from an owner's own fields
 * (the customer code, the household id and the identity key). Callers take either the full
 * lower-case hex digest or a fixed-length upper-case prefix, so the derivation lives here once
 * rather than being copied per caller.
 */
final class Hashes {

    private Hashes() {
    }

    /**
     * The full SHA-256 digest of {@code value} rendered as lower-case hex over the UTF-8 bytes
     * of the input (64 hex characters).
     */
    static String lowerHex(String value) {
        return hex(value);
    }

    /**
     * The first {@code length} characters of the SHA-256 digest of {@code value}, rendered
     * as upper-case hex over the UTF-8 bytes of the input.
     */
    static String upperHexPrefix(String value, int length) {
        return hex(value).substring(0, length).toUpperCase(Locale.ROOT);
    }

    private static String hex(String value) {
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
