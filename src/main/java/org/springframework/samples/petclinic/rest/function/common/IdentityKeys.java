package org.springframework.samples.petclinic.rest.function.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Derives the owner's {@code identityKey}, the single value all create-owner duplicate detection is
 * based on.
 *
 * <p>The key is the 64-character lower-case hex {@code SHA-256} of
 * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}, built from values that are
 * already in their canonical form by the time they reach here — the telephone normalized to E.164 and
 * the email lower-cased (re-lowered here defensively) with the last name reduced to its
 * {@link Soundex} code. A create is rejected with 409 Conflict only when a new owner's <em>whole</em>
 * key equals an existing owner's; because the telephone is part of the key, two owners who share a
 * last name and postcode but have different telephones have different keys and are both allowed (the
 * second is instead surfaced as a possible duplicate).
 */
public final class IdentityKeys {

    private IdentityKeys() {
    }

    /** The identity key of a persisted owner, from its stored telephone, email and last name. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identity key for the given components; a {@code null} component contributes an empty part. */
    public static String of(String telephone, String email, String lastName) {
        String input = safe(telephone) + "|" + safe(email).toLowerCase(Locale.ROOT) + "|"
                + Soundex.of(lastName);
        return sha256hex(input);
    }

    private static String sha256hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
