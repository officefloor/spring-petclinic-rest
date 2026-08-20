package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.function.common.Localities;

/**
 * The single derived identity used for owner duplicate detection.
 *
 * <p>An owner's {@code identityKey} is the lower-case SHA-256 hex digest (64 characters) of
 * {@code V2 + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, computed from
 * the owner's
 * already-normalized stored fields: the E.164 telephone (see {@link NormalizeOwnerTelephone}), the
 * lower-cased email (see {@link NormalizeOwnerEmail}), and the {@link Soundex} code of the last name,
 * all prefixed with the fixed {@link Localities#IDENTITY_VERSION_TAG V2 version tag} so version-2
 * keys never collide with the version-1 values. Two owners are duplicates only when their whole keys
 * are equal — consolidating what were once
 * separate telephone, email and household checks into this one comparison. Because the telephone is
 * part of the key, two owners with the same last name and postcode but different telephones have
 * different keys and are both allowed (the second is a {@link AssignPossibleDuplicate soft match}).
 */
public final class OwnerIdentityKey {

    private OwnerIdentityKey() {
    }

    /**
     * The identity key for an owner: SHA-256 hex over the V2 version tag, its stored (normalized)
     * telephone, lower-cased email and the Soundex of its last name.
     */
    public static String of(Owner owner) {
        String telephone = owner.getTelephone() == null ? "" : owner.getTelephone();
        String email = (owner.getEmail() == null || owner.getEmail().isBlank())
                ? "" : owner.getEmail().toLowerCase(Locale.ROOT);
        String lastNameSoundex = Soundex.of(owner.getLastName());
        return sha256Hex(Localities.IDENTITY_VERSION_TAG + "|" + telephone + "|" + email + "|"
                + lastNameSoundex);
    }

    /** The lower-case SHA-256 hex digest (64 characters) of {@code value}. */
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
            throw new IllegalStateException("SHA-256 unavailable", e);
        }
    }
}
