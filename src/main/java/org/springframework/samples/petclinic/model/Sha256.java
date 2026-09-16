package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The single definition of a truncated SHA-256 fingerprint: the leading upper-case hex
 * characters of the digest of a string. Shared by everything that fingerprints owner data
 * (customer code hash, household id) so they hash identically.
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
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02X", b & 0xFF));
            }
            return hex.substring(0, length);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required to derive a fingerprint", ex);
        }
    }
}
