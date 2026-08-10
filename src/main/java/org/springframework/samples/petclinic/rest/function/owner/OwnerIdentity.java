package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * Derives an owner's {@code identityKey} — the single value all duplicate detection is expressed
 * through. The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or
 * empty)}: a full-key match with an existing owner is a duplicate (409); any differing component (a
 * different telephone, email, or household) is a distinct identity that is allowed.
 *
 * <p>Not an OfficeFloor function class — a plain helper shared by the create-owner steps so the key
 * derived at the uniqueness check, the household id assigned to the entity, and the key returned in
 * the response are all computed the same way.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Assembles the identity key from its three components. {@code telephone} is expected in E.164
     * form and {@code email} already normalized (trimmed, lower-cased); a null/blank email or a null
     * householdId contributes an empty component.
     */
    public static String key(String telephone, String email, String householdId) {
        String tel = telephone == null ? "" : telephone;
        String mail = (email == null || email.isBlank()) ? "" : email;
        String hh = householdId == null ? "" : householdId;
        return tel + "|" + mail + "|" + hh;
    }

    /** Best-effort E.164 for a stored telephone; falls back to the raw value when it cannot form E.164. */
    public static String toE164(String telephone) {
        try {
            return TelephoneE164.toE164(telephone);
        }
        catch (InvalidTelephoneException ex) {
            return telephone;
        }
    }

    /** Trimmed, lower-cased email, or null when absent — matching {@link NormalizeOwnerEmail}. */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * Stable, shared household id: the first 16 upper-hex chars of SHA-256 over the canonical last
     * name and normalized address, so all members of a household derive the same value.
     */
    public static String householdId(String lastName, String address) {
        String canonicalLastName = canonicalName(lastName);
        String normalizedAddress = AddressNormalizer.normalize(address);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest((canonicalLastName + "\n" + normalizedAddress).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    /** Lower-cased, trimmed, with any run of whitespace collapsed to a single space. */
    private static String canonicalName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
