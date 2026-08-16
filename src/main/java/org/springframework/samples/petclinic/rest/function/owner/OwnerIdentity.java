package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The single derived identity of an owner used for duplicate detection.
 *
 * <p>The {@code identityKey} is the lower-case hex SHA-256 digest over
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. Two owners are
 * duplicates only when their WHOLE key is equal, so — because the telephone is part of the key —
 * two members of the same household (same last-name soundex and postcode) with different
 * telephones have different keys and are both allowed (the second is flagged a soft match by
 * {@link AssignPossibleDuplicate}, not rejected).
 *
 * <ul>
 *   <li>telephone — normalized to E.164 with {@link NormalizeOwnerTelephone#toE164(String)} so
 *       equivalent numbers in different formats collide; the raw value is used only as a
 *       fallback when it cannot form a valid E.164 number.</li>
 *   <li>email — lower-cased so addresses differing only in case collide; a null or blank email
 *       contributes an empty segment.</li>
 *   <li>lastName — reduced to its {@link Soundex} code so surnames that sound alike collide.</li>
 * </ul>
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the version-2 identity key: the hex SHA-256 of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName) + '|' + "V2"}. The
     * fixed {@code "V2"} tag is mixed in so no key equals its version-1 form.
     */
    public static String identityKey(String telephone, String email, String lastName) {
        String tel = NormalizeOwnerTelephone.toE164(telephone);
        if (tel == null) {
            tel = telephone == null ? "" : telephone;
        }
        String mail = (email == null || email.isBlank()) ? "" : email.toLowerCase();
        String key = tel + "|" + mail + "|" + Soundex.of(lastName) + "|" + OwnerIdentityVersion.TAG;
        return sha256Hex(key);
    }

    /** Full lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
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
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }
}
