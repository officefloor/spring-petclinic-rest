package org.springframework.samples.petclinic.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared SHA-256 hex encoding: the leading {@code bytes} bytes of {@code SHA-256(input)} rendered
 * as lower- or upper-case hexadecimal. Extracted so the several identity, household and customer-code
 * policies share one implementation instead of each repeating the digest-and-format loop.
 */
final class Sha256Hex {

    private Sha256Hex() {
    }

    /** Lower-case hex of the leading {@code bytes} bytes of {@code SHA-256(input)}. */
    static String lower(String input, int bytes) {
        return hex(input, bytes, "%02x");
    }

    /** Upper-case hex of the leading {@code bytes} bytes of {@code SHA-256(input)}. */
    static String upper(String input, int bytes) {
        return hex(input, bytes, "%02X");
    }

    private static String hex(String input, int bytes, String format) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes * 2);
            for (int i = 0; i < bytes; i++) {
                sb.append(String.format(format, digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
