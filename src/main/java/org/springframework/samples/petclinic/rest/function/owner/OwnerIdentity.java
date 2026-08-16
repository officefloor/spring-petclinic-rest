package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} — the single value all duplicate detection is expressed
 * through.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + householdId}. Two owners
 * are duplicates only when their <em>whole</em> keys are equal (see {@link CheckIdentityUnique}).
 * Because the telephone is part of the key, two members of the same household (same
 * {@code householdId}) with different telephones have different keys and are both allowed.
 *
 * <p>The three components are normalized so the key is stable: the telephone to E.164 form (see
 * {@link TelephoneE164}), the email lower-cased and trimmed, and a null household rendered as empty.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The identity key of a persisted owner, from its stored telephone, email and householdId. */
    public static String of(Owner owner) {
        return key(owner.getTelephone(), owner.getEmail(), owner.getHouseholdId());
    }

    /** Assemble the identity key from the three components, normalizing telephone and email. */
    public static String key(String telephone, String email, String householdId) {
        return normalizeTelephone(telephone) + "|" + normalizeEmail(email) + "|"
                + (householdId == null ? "" : householdId);
    }

    /**
     * The stable, shared household identifier for an owner's last name and postcode: the first 12
     * upper-case hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode} (last name
     * lower-cased with whitespace collapsed; postcode trimmed). Owners with the same last name and
     * postcode get the same value automatically, regardless of creation order, so the household is
     * keyed on {@code (lastName, postcode)}. Returns {@code null} when there is no postcode — an owner
     * without a postcode is not in any household.
     */
    public static String householdIdFor(String lastName, String postcode) {
        String normalizedPostcode = normalizePostcode(postcode);
        if (normalizedPostcode == null) {
            return null;
        }
        return sha256Hex(normalizeLastName(lastName) + "|" + normalizedPostcode, 12);
    }

    /**
     * The HASH8 half of an owner's region-and-hash {@code customerCode}: the first 8 upper-case hex
     * characters of SHA-256 over {@code normalizedTelephone + lastName}. The telephone is normalized
     * to E.164 form (null/invalid becomes empty) so the hash is stable across equivalent phone
     * formats; the last name is taken as stored.
     */
    public static String customerCodeHash(String telephone, String lastName) {
        return sha256Hex(normalizeTelephone(telephone) + (lastName == null ? "" : lastName), 8);
    }

    /** First {@code length} upper-case hex chars of SHA-256 over the UTF-8 bytes of {@code input}. */
    private static String sha256Hex(String input, int length) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, length).toUpperCase(Locale.ROOT);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Telephone in E.164 form for comparison; null/invalid becomes empty. */
    private static String normalizeTelephone(String telephone) {
        String e164 = TelephoneE164.normalize(telephone);
        return e164 == null ? "" : e164;
    }

    /** Email lower-cased and trimmed so comparison ignores case; null/blank becomes empty. */
    private static String normalizeEmail(String email) {
        if (email == null) {
            return "";
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** Last name lower-cased with runs of whitespace collapsed, so comparison ignores case/spacing. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /** Postcode trimmed; null or blank becomes null (no household). */
    private static String normalizePostcode(String postcode) {
        if (postcode == null) {
            return null;
        }
        String trimmed = postcode.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
