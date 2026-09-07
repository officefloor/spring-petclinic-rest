package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Single home for the SHA-256 hex hashing the create pipeline uses to derive stable identifiers.
 * Both {@link CustomerCode} (the {@code HASH8} segment of the customer code) and
 * {@link Household} (the shared household id) go through here, so the hashing stays defined in one
 * place.
 */
final class ShaHex {

    private ShaHex() {
    }

    /**
     * The first {@code length} UPPER-case hex characters of SHA-256 over the UTF-8 bytes of
     * {@code value}. Used to derive short, stable identifiers from a canonical input string.
     */
    static String upperPrefix(String value, int length) {
        return sha256Hex(value).substring(0, length).toUpperCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
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
