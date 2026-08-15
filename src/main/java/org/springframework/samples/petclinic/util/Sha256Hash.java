package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-256 helper for deriving short, stable identifiers. Hashes the UTF-8 bytes of the input and
 * exposes the leading hex characters in upper case, as used by the owner {@code memberId}
 * (its {@code HASH8} component is the first 8 upper-case hex characters of SHA-256).
 */
public final class Sha256Hash {

    private Sha256Hash() {
    }

    /** The first {@code n} UPPER-case hex characters of SHA-256 over the UTF-8 bytes of {@code s}. */
    public static String upperHex(String s, int n) {
        byte[] digest = digest(s);
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < digest.length && sb.length() < n; i++) {
            sb.append(String.format("%02X", digest[i]));
        }
        return sb.substring(0, n);
    }

    private static byte[] digest(String s) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
