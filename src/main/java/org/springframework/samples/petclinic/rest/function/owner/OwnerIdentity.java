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
     * The stable, shared household identifier for an owner's last name and address, normalizing both
     * the canonical way first (last name lower-cased with whitespace collapsed; address via
     * {@link AddressNormalizer}). Owners living at the same address under the same last name get the
     * same value regardless of creation order.
     */
    public static String householdIdFor(String lastName, String address) {
        return householdId(normalizeLastName(lastName), AddressNormalizer.normalize(address));
    }

    /** First 16 upper-case hex chars of SHA-256(lastName|address), over already-normalized inputs. */
    private static String householdId(String lastName, String address) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest((lastName + "|" + address).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.substring(0, 16).toUpperCase(Locale.ROOT);
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
}
