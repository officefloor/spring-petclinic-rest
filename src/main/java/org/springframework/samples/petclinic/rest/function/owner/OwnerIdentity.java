package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.OwnerRegion;

/**
 * Single source of truth for an owner's {@code identityKey} — the value that both the owner mapper
 * returns and the create pipeline's duplicate detection compares on. The key is the lower-case hex
 * SHA-256 of {@code region + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)},
 * where {@code region} is the version-2 identity region code (see {@link OwnerRegion#identityRegion}),
 * the telephone is reduced to E.164 through {@link OwnerTelephone#toE164}, the email is lower-cased
 * through {@link OwnerEmail#normalize}, and the last name is reduced to its phonetic code through
 * {@link OwnerSoundex#soundex}. A missing telephone or email contributes an empty segment.
 *
 * <p>Because the telephone is part of the key, two owners who share a last name and postcode but
 * carry different telephones have <em>different</em> identity keys: they are not a hard duplicate.
 * That weaker, phonetic-name-plus-postcode overlap is handled as a soft match by
 * {@link FlagOwnerPossibleDuplicate}.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    /**
     * Builds the version-2 identity key from the owner's version-2 region code and its three raw
     * parts: the submitted/stored telephone (normalized to E.164 here), the email (lower-cased here)
     * and the last name (reduced to its Soundex code here). The region and the three normalized parts
     * are joined with {@code '|'} and hashed with SHA-256; a telephone or email that is {@code null}
     * or cannot normalize contributes an empty segment. The {@code V2}-tagged region (see
     * {@link OwnerRegion#identityRegion}) means no key matches the one version 1 would have produced.
     */
    public static String identityKey(String region, String telephone, String email, String lastName) {
        String normalizedTelephone = OwnerTelephone.toE164(telephone);
        String normalizedEmail = OwnerEmail.normalize(email);
        String raw = region + "|"
                + (normalizedTelephone == null ? "" : normalizedTelephone) + "|"
                + (normalizedEmail == null ? "" : normalizedEmail) + "|"
                + OwnerSoundex.soundex(lastName);
        return sha256Hex(raw);
    }

    /**
     * The identity key of an already-stored owner, read from its persisted fields. The single
     * {@code Owner}-to-key definition used by the owner mapper and by duplicate detection.
     */
    public static String identityKey(Owner owner) {
        String region = OwnerRegion.identityRegion(owner.getCity(), owner.getPostcode());
        return identityKey(region, owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** Lower-case hex SHA-256 of the UTF-8 bytes of {@code value}. */
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
