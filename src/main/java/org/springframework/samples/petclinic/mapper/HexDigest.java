package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared SHA-256 hex-digest helper for the identifiers derived on create. The customer code (see
 * {@link IdentityKeyResolver#deriveCustomerCode}) and the household id (see
 * {@link HouseholdResolver#deriveHouseholdId}) are both a prefix of a SHA-256 digest, so both express the same hashing
 * in one place here. Kept a static utility like {@link LocalityResolver} and {@link MembershipLevelResolver}, since it
 * derives purely from its argument.
 */
public final class HexDigest {

    private HexDigest() {
    }

    /**
     * Returns the first {@code chars} upper-case hex characters of the SHA-256 digest of the UTF-8 bytes of
     * {@code value}.
     *
     * @param value the value to hash
     * @param chars the number of leading hex characters to return
     * @return the leading upper-case hex characters of the digest
     */
    public static String upperHexPrefix(String value, int chars) {
        StringBuilder hex = new StringBuilder();
        for (byte b : digest(value)) {
            hex.append(String.format("%02X", b));
        }
        return hex.substring(0, chars);
    }

    /**
     * Returns the full 64-character lower-case hex rendering of the SHA-256 digest of the UTF-8 bytes of {@code value}.
     * Used by the owner {@code identityKey} (see {@link IdentityKeyResolver#deriveIdentityKey}), which is expressed as
     * the whole digest rather than a leading prefix.
     *
     * @param value the value to hash
     * @return the 64-character lower-case hex digest
     */
    public static String lowerHex(String value) {
        StringBuilder hex = new StringBuilder();
        for (byte b : digest(value)) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    /**
     * Computes the raw SHA-256 digest of the UTF-8 bytes of {@code value}. Isolated here so every hex rendering shares
     * one notion of "the digest" (and one place that handles a missing SHA-256 provider) rather than repeating the
     * {@link MessageDigest} plumbing.
     *
     * @param value the value to hash
     * @return the SHA-256 digest bytes
     */
    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is not available", ex);
        }
    }
}
