package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single source of truth for the SHA-256 hex encoding used to derive stable identifiers, so the
 * identity key (see {@link OwnerIdentity}) and the household id (see {@link HouseholdKey}) can
 * never drift in how they hash. Hashes the UTF-8 bytes of the input and lower-cases the hex.
 */
final class Hashing {

    private Hashing() {
    }

    /** Full lower-case hex SHA-256 (64 characters) of the UTF-8 bytes of {@code value}. */
    static String sha256Hex(String value) {
        return encode(digest(value), 32);
    }

    /** The first {@code bytes} bytes of SHA-256({@code value}) as lower-case hex ({@code 2*bytes} chars). */
    static String sha256HexPrefix(String value, int bytes) {
        return encode(digest(value), bytes);
    }

    private static byte[] digest(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private static String encode(byte[] digest, int bytes) {
        StringBuilder sb = new StringBuilder(bytes * 2);
        for (int i = 0; i < bytes; i++) {
            sb.append(String.format("%02x", digest[i]));
        }
        return sb.toString();
    }
}
