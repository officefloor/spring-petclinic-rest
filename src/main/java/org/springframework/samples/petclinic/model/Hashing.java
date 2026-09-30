package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single definition of the lower-case hex SHA-256 primitive shared by the owner-identity
 * derivation helpers — {@link OwnerIdentity} (the identityKey) and the household hash in the
 * request-handling layer — so the hashing lives in one place rather than being copied. Lives in
 * {@code model} beside {@link Owner} so both the mapping and request-handling layers can depend
 * on it without a package cycle.
 */
public final class Hashing {

    private Hashing() {
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(Character.forDigit((b >> 4) & 0xF, 16));
                sb.append(Character.forDigit(b & 0xF, 16));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
