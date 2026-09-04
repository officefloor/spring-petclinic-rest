package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Derives the single duplicate-detection key for an owner: the SHA-256 hex of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners
 * are duplicates only when their whole identity keys match. Because the telephone is part
 * of the key, two members of one household with different telephones get different keys
 * and are both allowed; only an exact full-key match is a duplicate.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(String telephone, String email, String lastName) {
        String key = "V2|" + (telephone == null ? "" : telephone) + "|"
                + (email == null ? "" : email.toLowerCase()) + "|" + Soundex.of(lastName);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
