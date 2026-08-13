package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived key that all owner duplicate detection is expressed through: the lower-case
 * SHA-256 hex digest (64 characters) of {@code <normalizedTelephone>|<lowerEmail>|<soundex(lastName)>}.
 * The telephone is the normalized E.164 form, the email is the lower-cased address (empty when
 * absent) and the last-name component is its {@link OwnerSoundex} code, so the key groups
 * phonetically-equal surnames. Two owners are duplicates only when their whole identityKey is equal —
 * because the telephone is part of the key, owners with the same last name and postcode but different
 * telephones have different keys and are both allowed (flagged instead as a soft match).
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /** The identity key of a stored owner, from its telephone, email and last name. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identity key for the given normalized telephone, email and last name. */
    public static String of(String telephone, String email, String lastName) {
        String material = safe(telephone) + "|" + lowerEmail(email) + "|" + OwnerSoundex.of(lastName);
        return sha256hex(material);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String lowerEmail(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
    private static String sha256hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
