package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey}: the SHA-256 hex digest of normalized telephone + '|' +
 * (lower-cased email or empty) + '|' + {@link Soundex} of the last name. Duplicate detection is a
 * single equality on this key, so two owners collide only when telephone, email and the phonetic
 * last name all match; sharing only a household (different telephones) yields different keys.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    public static String of(String telephone, String email, String lastName) {
        String lowerEmail = email == null || email.isBlank() ? "" : email.toLowerCase(Locale.ROOT);
        return sha256hex("V2|" + (telephone == null ? "" : telephone) + "|" + lowerEmail + "|" + Soundex.of(lastName));
    }

    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
