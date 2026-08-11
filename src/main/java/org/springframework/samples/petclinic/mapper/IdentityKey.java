package org.springframework.samples.petclinic.mapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives an owner's {@code identityKey} at version 2: the single duplicate-detection key that all
 * owner duplicate rules are now expressed through. The key is the 64-character SHA-256 hex digest of
 * {@code 'V2' + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where the
 * fixed {@code 'V2'} version tag is mixed into the hashed input (so every key differs from its
 * version-1 value and no version-1 value is produced again), the telephone is the stored (already
 * E.164-normalized) value, the email is lower-cased or an empty segment when absent, and
 * {@code soundex(lastName)} is the American Soundex code of the last name (see {@link Soundex}).
 *
 * <p>Two owners are duplicates only when their <em>whole</em> identityKey is equal: because the
 * telephone is part of the key, two owners with the same last name (hence the same soundex) and
 * postcode but <em>different</em> telephones have different keys and are both allowed — the
 * second is flagged a soft match, not rejected. Kept out of {@link OwnerMapper} so MapStruct does
 * not mistake the helper for an implicit mapping method.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The fixed version-2 tag mixed into the hashed identity-key input. */
    private static final String VERSION_TAG = "V2";

    /** The version-2 duplicate-detection identity key for {@code owner}: SHA-256 hex over
     *  {@code 'V2'|normalizedTelephone|lowerEmail|soundex(lastName)}. */
    public static String of(Owner owner) {
        String raw = VERSION_TAG + "|" + segment(owner.getTelephone()) + "|" + email(owner.getEmail())
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

    private static String segment(String value) {
        return value == null ? "" : value;
    }

    private static String email(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }
}
