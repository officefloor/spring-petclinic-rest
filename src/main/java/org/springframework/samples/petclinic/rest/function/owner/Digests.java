package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The one place that turns a string into its SHA-256 hex digest. Several owner
 * derivations are built on the same hash — {@link Households} (the {@code householdId}
 * prefix) and {@link CustomerCodes} (the {@code HASH8} segment) both fold their key
 * through it — so keeping the encoding here means every one of them agrees on the exact
 * bytes and hex form instead of each hand-rolling {@link MessageDigest}.
 *
 * <p>{@link #sha256Hex(String)} is the primitive: the full, lower-case hex string. Callers
 * that want a shorter, upper-case identifier take a {@code substring} and upper-case it, so
 * the slicing and casing stay a decision of the derivation, not of the hash.
 */
public final class Digests {

    private Digests() {
    }

    /** The full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String sha256Hex(String value) {
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
