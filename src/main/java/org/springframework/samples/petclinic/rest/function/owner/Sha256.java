package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The single definition of the project's SHA-256 hashing over the UTF-8 bytes of a string.
 * The owner {@link MemberId member id} and {@link Household household identifier} take the
 * first {@code length} upper-case hex characters; the owner {@link IdentityKey} takes the
 * full lower-case digest — all share one implementation rather than each rolling their own.
 *
 * <p>Pure function of its inputs; no state.
 */
final class Sha256 {

    private Sha256() {
    }

    /** The first {@code length} upper-case hex characters of SHA-256 over {@code value}. */
    static String hex(String value, int length) {
        byte[] digest = digest(value);
        StringBuilder hex = new StringBuilder(length);
        for (int i = 0; hex.length() < length; i++) {
            hex.append(String.format("%02X", digest[i]));
        }
        return hex.substring(0, length);
    }

    /** The full 64-character lower-case hex SHA-256 of {@code value}. */
    static String hexLower(String value) {
        byte[] digest = digest(value);
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
