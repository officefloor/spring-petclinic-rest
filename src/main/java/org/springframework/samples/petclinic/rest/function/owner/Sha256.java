package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Shared SHA-256 hex encoding for the {@code POST /owners} pipeline. Both the owner
 * {@link OwnerIdentity identityKey} (full, lower-case) and the {@link OwnerHousehold} household id
 * (first 12 characters, upper-cased) are the hex digest of a composed string; this centralises the
 * digest-and-encode mechanism so those callers choose only the case/length they want rather than
 * each re-implementing it.
 */
final class Sha256 {

    private static final char[] LOWER_HEX = "0123456789abcdef".toCharArray();

    private Sha256() {
    }

    /** The lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    static String hex(String value) {
        byte[] digest = digest(value);
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(LOWER_HEX[(b >> 4) & 0xF]).append(LOWER_HEX[b & 0xF]);
        }
        return sb.toString();
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
