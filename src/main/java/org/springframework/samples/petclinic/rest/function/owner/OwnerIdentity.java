package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single {@code identityKey} used for all owner duplicate detection.
 *
 * <p>The key is the lower-case SHA-256 hex digest of {@code regionCodeV2 + '|' +
 * normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, where the leading
 * version-2 region code (derived from the postcode) rederives every key for version 2.
 * A create is a duplicate only when a new owner's
 * WHOLE identityKey equals an existing owner's — the former separate telephone, email and
 * household checks are now expressed through this one hashed key. Because the telephone is
 * part of the key, two owners with the same last name and postcode but different
 * telephones have different keys and are both allowed; only an exact full-key match is a
 * duplicate (they may still be flagged a soft possible duplicate — see
 * {@link AssignOwnerPossibleDuplicate}).
 *
 * <p>Both sides are canonicalized the same way so a new owner and the stored owners
 * compare on equal footing: telephone via {@link E164Telephone}, email trimmed and
 * lower-cased, and the last name folded to its {@link Soundex} code (empty when absent).
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /** The owner's derived identity key: a 64-character lower-case SHA-256 hex digest. */
    public static String identityKey(Owner owner) {
        // Version-2: mix the version-2 region code into the key so it is rederived and no
        // value produced under version 1 recurs. Both sides of a duplicate comparison
        // canonicalize identically, so the check stays consistent.
        String raw = PostcodeRegions.regionCodeV2(owner.getPostcode()) + "|" + telephone(owner)
                + "|" + email(owner) + "|" + Soundex.encode(owner.getLastName());
        return sha256hex(raw);
    }

    private static String telephone(Owner owner) {
        String e164 = E164Telephone.toE164OrNull(owner.getTelephone());
        if (e164 != null) {
            return e164;
        }
        return owner.getTelephone() == null ? "" : owner.getTelephone();
    }

    private static String email(Owner owner) {
        String value = owner.getEmail();
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? "" : trimmed.toLowerCase(Locale.ROOT);
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code raw}. */
    private static String sha256hex(String raw) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
