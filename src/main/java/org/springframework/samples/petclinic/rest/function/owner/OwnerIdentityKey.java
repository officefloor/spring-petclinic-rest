package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;
import org.springframework.samples.petclinic.util.IdentityVersion;

/**
 * Shared derivation of an owner's {@code identityKey} — the single value that all duplicate detection
 * is now expressed through. Under the version-2 identity a fixed {@code "V2"} tag is mixed in, so the
 * key is the lower-case hex SHA-256 digest of
 * {@code normalizedTelephone + "|" + lowerEmail + "|" + soundex(lastName) + "|" + "V2"}, where:
 *
 * <ul>
 * <li>{@code normalizedTelephone} is the E.164 form (see {@link TelephoneE164});</li>
 * <li>{@code lowerEmail} is the lower-cased email, or empty when none is supplied; and</li>
 * <li>{@code soundex(lastName)} is the Soundex code of the last name (see {@link Soundex}).</li>
 * </ul>
 *
 * <p>Two owners are duplicates only when their <em>whole</em> keys are equal. Because the telephone is
 * part of the key, two owners who share a last name and postcode but carry different telephones have
 * different keys and are both allowed (the weaker overlap is a soft match, see
 * {@link AssignPossibleDuplicate}). The postcode is not part of the key, so a household is no longer a
 * hard-duplicate factor.
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * Identity key for an incoming create-owner request (after {@link ValidateNewOwner}). The last
     * name is taken straight from the request, so the key matches the one the owner will carry once
     * saved.
     */
    public static String forRequest(OwnerFieldsDto request) {
        return build(request.getTelephone(), request.getEmail(), request.getLastName());
    }

    /** Identity key for an existing, stored owner. */
    public static String forOwner(Owner owner) {
        return build(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    private static String build(String telephone, String email, String lastName) {
        String raw = normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|"
                + Soundex.soundex(lastName) + "|" + IdentityVersion.TAG;
        return sha256hex(raw);
    }

    /** Lower-case hex SHA-256 digest of the UTF-8 bytes of {@code value} (64 characters). */
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
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    /** E.164 form when the value can form one, otherwise the raw value so it only matches itself. */
    private static String normalizeTelephone(String telephone) {
        if (telephone == null) {
            return "";
        }
        try {
            return TelephoneE164.normalize(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone;
        }
    }

    /** Lower-cased email, or empty when none is supplied. */
    private static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}
