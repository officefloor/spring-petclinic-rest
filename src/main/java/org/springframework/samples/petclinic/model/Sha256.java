package org.springframework.samples.petclinic.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Lower-case hex {@code SHA-256} of a string's UTF-8 bytes — the one hashing routine the owner
 * derivations share, so the hex encoding cannot silently diverge between them. Used by
 * {@link OwnerIdentity} (the {@code identityKey}) and {@link org.springframework.samples.petclinic.rest.function.owner.OwnerHousehold}
 * (the {@code householdId}).
 */
public final class Sha256 {

    private Sha256() {
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    public static String hex(String value) {
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
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
