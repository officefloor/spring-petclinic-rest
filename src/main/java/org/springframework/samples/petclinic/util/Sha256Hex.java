package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes a hexadecimal SHA-256 digest of a string, either as a truncated upper-case prefix
 * ({@link #upperHexPrefix}) or as the full 64-character lower-case digest ({@link #lowerHex}).
 * Used wherever a stable hash is needed over owner data: the hash segment of the customer code and
 * the shared household identifier use the prefix form, while the owner identity key uses the full
 * lower-case form.
 */
public final class Sha256Hex {

    private Sha256Hex() {
    }

    /**
     * The first {@code chars} upper-case hexadecimal characters of the SHA-256 digest of the UTF-8
     * bytes of {@code value} (e.g. {@code chars = 8} returns the first 4 bytes as 8 hex characters).
     *
     * @param value the string to hash
     * @param chars the number of leading hexadecimal characters to return (0 to 64)
     * @return the truncated upper-case hexadecimal digest
     */
    public static String upperHexPrefix(String value, int chars) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(chars);
            for (int i = 0; i < digest.length && sb.length() < chars; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.substring(0, chars);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }

    /**
     * The full 64-character lower-case hexadecimal SHA-256 digest of the UTF-8 bytes of
     * {@code value}. Used for the owner identity key, which is compared in its entirety.
     *
     * @param value the string to hash
     * @return the 64-character lower-case hexadecimal digest
     */
    public static String lowerHex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
