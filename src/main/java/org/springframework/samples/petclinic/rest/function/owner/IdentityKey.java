package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Derives an owner's {@code identityKey}, the single value all duplicate detection is expressed
 * through:
 *
 * <pre>identityKey = SHA-256 hex over (normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))</pre>
 *
 * <p>The three components are joined with {@code '|'} and hashed with SHA-256, yielding a 64-character
 * lower-case hex string. The telephone is normalized to its E.164 form (see {@link E164Telephone}); a
 * value that cannot form a valid E.164 number is used verbatim so the key stays defined for every
 * stored owner. The email contributes its lower-cased form, or the empty string when absent. The last
 * name contributes its {@link Soundex} code, so phonetically identical surnames share this component.
 *
 * <p>Two owners are duplicates only when their WHOLE identityKey is equal. Because the telephone is
 * part of the key, owners with the same surname (and postcode) but different telephones have
 * different keys and are both allowed — they are flagged as a soft match rather than rejected.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    public static String of(Owner owner) {
        String raw = normalizeTelephone(owner.getTelephone()) + "|" + normalizeEmail(owner.getEmail())
                + "|" + Soundex.of(owner.getLastName());
        return sha256Hex(raw);
    }

    private static String sha256Hex(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String normalizeTelephone(String telephone) {
        try {
            return E164Telephone.normalize(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone == null ? "" : telephone;
        }
    }

    private static String normalizeEmail(String email) {
        return email == null || email.isBlank() ? "" : email.toLowerCase();
    }
}
