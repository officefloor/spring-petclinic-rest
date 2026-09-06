package org.springframework.samples.petclinic.rest.function.owner;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the single {@code identityKey} that every owner duplicate check is expressed through: the
 * lower-case hex SHA-256 of
 * {@code identifierRegion + '|' + normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}.
 * The version-2 {@link MemberIds#identifierRegion(Owner) identifier region} leads the input, so the
 * key is rederived under version 2 and never reproduces a version-1 value; existing owners recompute
 * their key the same way, so duplicate detection stays consistent.
 *
 * <p>Two owners are duplicates only when their WHOLE keys are equal. Because the normalized telephone
 * is part of the key, two owners with the same last name and postcode but DIFFERENT telephones have
 * different keys and are both allowed (they become a soft match instead — see
 * {@link AssignPossibleDuplicate}). The telephone is already normalized to E.164 and the email to
 * lower case by {@link BuildOwner}; the surname contributes its {@link Soundex} code so spelling
 * variants collapse together. A missing telephone or email contributes an empty segment.
 */
public final class OwnerIdentity {

    private OwnerIdentity() {
    }

    public static String key(Owner owner) {
        String input = MemberIds.identifierRegion(owner) + '|' + segment(owner.getTelephone()) + '|'
                + lower(owner.getEmail()) + '|' + Soundex.of(owner.getLastName());
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
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
        return value == null || value.isBlank() ? "" : value;
    }

    private static String lower(String value) {
        return value == null || value.isBlank() ? "" : value.toLowerCase(Locale.ROOT);
    }
}
