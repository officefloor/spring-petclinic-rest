package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Single source of truth for an owner's <em>identity</em>. All duplicate detection on the create
 * endpoint is expressed through one derived {@code identityKey}:
 *
 * <pre>identityKey = normalizedTelephone + '|' + (email or empty) + '|' + householdId</pre>
 *
 * <p>A new owner is a duplicate (409) only when its whole {@code identityKey} equals an existing
 * owner's — see {@link CheckIdentityUnique}. Because the normalized telephone is part of the key,
 * two members of the same household (same {@code householdId}) with different telephones have
 * different keys and are both allowed; only an exact full-key match collides.
 *
 * <p>The {@code householdId} is the stable identifier shared by owners living in the same household
 * (same normalized last name and address): the first 12 upper-cased hex characters of the SHA-256 of
 * {@code '<lastName>|<address>'}. {@link AssignHouseholdId} assigns it and {@link OwnerMapper} returns
 * both it and the {@code identityKey}.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * The shared {@code householdId} derived from the last name and address: the first 12 upper-cased
     * hex characters of SHA-256 of {@code '<lastName>|<address>'}, with the last name normalized
     * (lower-cased, runs of whitespace collapsed, trimmed) and the address normalized to its canonical
     * form (see {@link AddressNormalizer}).
     */
    public static String householdId(String lastName, String address) {
        String key = normalizeName(lastName) + "|" + AddressNormalizer.normalize(address);
        return shaHex(key, 12);
    }

    /**
     * The derived {@code identityKey}: {@code normalizedTelephone + '|' + (email or empty) + '|' +
     * householdId}. A blank/absent email contributes the empty string.
     */
    public static String identityKey(String telephone, String email, String householdId) {
        String tel = telephone == null ? "" : telephone;
        String em = (email == null || email.isBlank()) ? "" : email;
        String hh = householdId == null ? "" : householdId;
        return tel + "|" + em + "|" + hh;
    }

    /** Lower-cases and collapses runs of whitespace to a single space, trimmed. Used for last name. */
    private static String normalizeName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    /** First {@code n} upper-case hex characters of SHA-256 of {@code value}. */
    private static String shaHex(String value, int n) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02X", b));
            }
            return sb.substring(0, n);
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
