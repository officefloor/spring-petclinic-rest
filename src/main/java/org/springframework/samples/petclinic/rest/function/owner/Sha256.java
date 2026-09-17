package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The single definition of the project's SHA-256 hashing: the first {@code length} upper-case
 * hex characters of SHA-256 over the UTF-8 bytes of a string. Both the owner
 * {@link CustomerCode customer code} and the {@link Household household identifier} are derived
 * this way, so they share one implementation rather than each rolling their own digest.
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

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
    }
}
