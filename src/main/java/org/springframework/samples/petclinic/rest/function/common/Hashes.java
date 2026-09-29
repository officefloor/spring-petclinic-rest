package org.springframework.samples.petclinic.rest.function.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * SHA-256 helper used to derive an owner's {@code customerCode} hash segment. Pinned so both the
 * Spring and OfficeFloor arms produce identical values: the digest is taken over the UTF-8 bytes and
 * rendered as lower-case hex, from which callers take the leading, upper-cased characters.
 *
 * <p>Kept out of the function classes themselves because OfficeFloor requires each function class to
 * expose exactly one public method.
 */
public final class Hashes {

    private Hashes() {
    }

    /** The full lower-case hex SHA-256 of the UTF-8 bytes of {@code s}. */
    public static String sha256Hex(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** The first {@code n} upper-case hex characters of SHA-256 over {@code s}. */
    public static String sha256UpperHex(String s, int n) {
        return sha256Hex(s).substring(0, n).toUpperCase(Locale.ROOT);
    }
}
