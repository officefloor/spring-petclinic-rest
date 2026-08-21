package org.springframework.samples.petclinic.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} - the single value all duplicate detection is expressed
 * through. It is the lower-case hex {@code SHA-256} of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where the telephone is
 * the owner's stored E.164 form, the email is its stored lower-cased form (empty when absent) and
 * the last name is reduced to its {@link Soundex} code. Two owners are duplicates when, and only
 * when, their identity keys are equal; because the telephone is part of the key, household members
 * with different telephones have different keys and are not hard duplicates.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * @param owner the owner whose identity key to derive.
     * @return the 64-character lower-case hex SHA-256 identity key.
     */
    public static String key(Owner owner) {
        String raw = IdentityVersion.TAG + "|" + segment(owner.getTelephone()) + "|"
                + segment(owner.getEmail()).toLowerCase() + "|" + Soundex.of(owner.getLastName());
        return sha256hex(raw);
    }

    private static String segment(String value) {
        return value == null ? "" : value;
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
