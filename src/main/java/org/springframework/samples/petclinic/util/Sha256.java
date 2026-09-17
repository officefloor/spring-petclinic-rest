package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared SHA-256 hex helper. A pure function of its input with no dependency on other owners, used
 * wherever the application needs a stable, opaque token derived from a value (the HASH8 portion of
 * the {@link MemberId}, the household id in
 * {@link org.springframework.samples.petclinic.rest.function.owner.HouseholdNormalizer}).
 */
public final class Sha256 {

    private Sha256() {
    }

    /** The full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
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

    /** The first {@code length} upper-case hex characters of the SHA-256 of {@code value}. */
    public static String prefix(String value, int length) {
        return hex(value).substring(0, length).toUpperCase();
    }
}
