package org.springframework.samples.petclinic.util;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single derived identity of an owner: the SHA-256 hex digest over the owner's
 * normalized telephone, lower-cased email and the {@link Soundex} code of its last name,
 * joined by {@code '|'}. The telephone and email are already canonical by the time a key is
 * built — the create steps normalize them, and stored owners hold the normalized values —
 * and the email is lower-cased again here so the key is stable regardless.
 *
 * <p>This is the identity that duplicate detection keys off: a create whose key matches a
 * non-deleted owner's is a 409 (see
 * {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueIdentity}).
 * The same value is exposed on the response mapper for callers.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key for the given owner, from its stored telephone, email and last name. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /**
     * The identity key for the given parts: the SHA-256 hex digest of
     * {@code normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName)}. A null or
     * blank telephone or email contributes an empty segment and a blank last name an empty
     * Soundex, so an owner missing one still has a well-formed 64-hex key.
     */
    public static String of(String telephone, String email, String lastName) {
        return Sha256.hex(orEmpty(telephone) + '|' + lowerEmail(email) + '|' + Soundex.of(lastName));
    }

    private static String lowerEmail(String email) {
        return orEmpty(email).toLowerCase(Locale.ROOT);
    }

    private static String orEmpty(String value) {
        return (value == null || value.isBlank()) ? "" : value;
    }
}
