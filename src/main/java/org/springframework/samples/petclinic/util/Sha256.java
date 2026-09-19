package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * SHA-256 hashing rendered as hexadecimal. Kept separate from any one caller so the
 * digest algorithm is defined once and reused wherever a stable hex fingerprint of a
 * string is needed (e.g. the hash component of an owner's customer code).
 */
public final class Sha256 {

    private Sha256() {
    }

    /**
     * The first {@code length} upper-case hexadecimal characters of the SHA-256 digest of
     * the UTF-8 bytes of {@code value}.
     *
     * @param value  the string to hash
     * @param length the number of leading hex characters to return
     * @return {@code length} upper-case hex characters
     */
    public static String upperHexPrefix(String value, int length) {
        byte[] digest = digest(value);
        StringBuilder hex = new StringBuilder(length);
        for (int i = 0; hex.length() < length; i++) {
            hex.append(String.format(Locale.ROOT, "%02X", digest[i]));
        }
        return hex.substring(0, length);
    }

    /**
     * The full SHA-256 digest of the UTF-8 bytes of {@code value} rendered as 64 lower-case
     * hexadecimal characters.
     *
     * @param value the string to hash
     * @return 64 lower-case hex characters
     */
    public static String lowerHex(String value) {
        byte[] digest = digest(value);
        StringBuilder hex = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            hex.append(String.format(Locale.ROOT, "%02x", b));
        }
        return hex.toString();
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is required but unavailable", ex);
        }
    }
}
