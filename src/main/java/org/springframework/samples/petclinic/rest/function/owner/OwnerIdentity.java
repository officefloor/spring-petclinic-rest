package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * The single source of truth for an owner's derived {@code identityKey} and its parts. All duplicate
 * detection is now expressed through this one key:
 *
 * <pre>identityKey = normalizedTelephone + '|' + (email or empty) + '|' + householdId</pre>
 *
 * <p>Two owners are duplicates only when their <em>whole</em> identityKey is equal. Because the
 * telephone is part of the key, two members of the same household (same householdId) with different
 * telephones have different identityKeys and are both allowed; only an exact full-key match is a
 * duplicate.
 *
 * <p>The household component is the deterministic id derived from the normalized lastName and address
 * (see {@link #deriveHouseholdId}); it is the empty string for an owner with no shared household, so
 * two solo owners with the same telephone and email collide.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The identityKey from its already-known parts. Telephone is canonicalized to E.164 and email is
     * lower-cased so differing formats or case do not hide a match; a null household is the empty
     * string.
     */
    public static String key(String telephone, String email, String householdId) {
        return canonicalTelephone(telephone) + '|' + normalizedEmail(email) + '|'
                + (householdId == null ? "" : householdId);
    }

    /**
     * The E.164 form used for comparison. A value that is already E.164 is returned unchanged; a value
     * that predates E.164 storage is normalized on the fly, falling back to its bare digits if it
     * cannot form valid E.164.
     */
    public static String canonicalTelephone(String value) {
        try {
            return OwnerTelephone.toE164(value);
        }
        catch (InvalidTelephoneException ex) {
            return value == null ? "" : value.replaceAll("\\D", "");
        }
    }

    /** The email lower-cased and trimmed, or the empty string when null or blank. */
    public static String normalizedEmail(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim().toLowerCase();
    }

    /** Lower-cased, trimmed, with runs of whitespace collapsed to a single space. */
    public static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** {@code H-} plus the first 12 upper-case hex chars of SHA-256(lastName + '|' + address). */
    public static String deriveHouseholdId(String normalizedLastName, String normalizedAddress) {
        return "H-" + sha256Hex(normalizedLastName + '|' + normalizedAddress, 12);
    }

    /**
     * The first {@code hexChars} upper-case hex characters of SHA-256 over the UTF-8 bytes of
     * {@code input} — the shared primitive behind every hash-derived identity part.
     */
    static String sha256Hex(String input, int hexChars) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hexChars);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
                if (sb.length() >= hexChars) {
                    break;
                }
            }
            return sb.substring(0, hexChars);
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
