package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

/**
 * Derives an owner's {@code identityKey} &mdash; the single value all duplicate detection is now
 * expressed through.
 *
 * <p>The key is {@code normalizedTelephone + '|' + (email or empty) + '|' + (householdId or empty)}.
 * Two owners are duplicates only when their <em>whole</em> keys are equal: because the telephone is
 * part of the key, two members of the same household (same {@code householdId}) with different
 * telephones have different keys and are both allowed.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the identity key from an owner's parts. The telephone is expected to already be in
     * E.164 form (as stored); the email is compared lower-cased and a null/blank email or
     * {@code householdId} contributes an empty segment.
     */
    public static String key(String telephone, String email, String householdId) {
        String tel = telephone == null ? "" : telephone;
        String em = (email == null || email.isBlank()) ? "" : email.trim().toLowerCase(Locale.ROOT);
        String hh = householdId == null ? "" : householdId;
        return tel + "|" + em + "|" + hh;
    }

    /**
     * The hash segment of an owner's {@code '<REGION>-<HASH8>'} customer code: the first 8
     * upper-case hex characters of SHA-256 over the normalised telephone concatenated with the
     * last name. The telephone is expected to already be in E.164 form (as stored).
     */
    public static String customerHash(String normalizedTelephone, String lastName) {
        String tel = normalizedTelephone == null ? "" : normalizedTelephone;
        String last = lastName == null ? "" : lastName;
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest((tel + last).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(8);
            for (int i = 0; i < 4; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Stable 16-char upper-case hex identifier derived from a household's last name and address. */
    public static String householdId(String lastName, String address) {
        try {
            String key = normalizeName(lastName) + " " + AddressNormalizer.normalize(address);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(16);
            for (int i = 0; i < 8; i++) {
                sb.append(String.format("%02X", digest[i]));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Lower-case, trim, and collapse internal whitespace runs to a single space. */
    static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
