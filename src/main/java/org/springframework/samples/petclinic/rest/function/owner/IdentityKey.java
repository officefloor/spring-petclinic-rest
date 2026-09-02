package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the single value duplicate detection is expressed
 * through. It is the SHA-256 hex digest of {@code normalizedTelephone + '|' + (email or empty) +
 * '|' + soundex(lastName)}, where the telephone is E.164 (see {@link E164Telephone}), the email is
 * lower-cased (empty when absent) and the last name is reduced to its {@link Soundex} code. Because
 * the telephone is part of the key, two members of one household with different telephones have
 * different keys and are both allowed; only owners that collide on the key are duplicates.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        return sha256Hex(telephone(owner) + '|' + email(owner) + '|' + Soundex.of(owner.getLastName()));
    }

    static String telephone(Owner owner) {
        String telephone = E164Telephone.toE164(owner.getTelephone());
        return telephone == null ? "" : telephone;
    }

    static String email(Owner owner) {
        String email = owner.getEmail();
        return (email == null || email.isBlank()) ? "" : email.toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(64);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
