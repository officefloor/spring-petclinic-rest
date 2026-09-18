package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner's <em>identity key</em>: the value that decides when
 * two owners are the same person. It is the SHA-256 hash of the owner's normalized telephone,
 * lower-cased email and the {@link Soundex} code of its last name, so a duplicate is a match
 * of the whole hashed key rather than of any single field, and two owners whose last names
 * merely sound alike still collide.
 *
 * <p>Key = {@code sha256Hex(normalizedTelephone + '|' + lowerEmail + '|' + soundex(lastName))},
 * as the full 64-character lower-case hex digest. The telephone and email are already stored
 * in canonical form ({@link NormalizeOwnerTelephone} stores the E.164 telephone,
 * {@link NormalizeOwnerEmail} the lower-cased email), so the key is built from the owner's
 * stored fields; email is lower-cased defensively so the comparison is case-insensitive.
 *
 * <p>Exposed as a derived, read-only owner field and used by the create endpoint's duplicate
 * block (see {@link EnsureUniqueIdentity}): two owners collide only when their whole keys
 * match, so owners sharing a last name and postcode but with a different telephone or email
 * are admitted as a {@link PossibleDuplicate soft match} rather than rejected.
 */
public final class IdentityKey {

    private IdentityKey() {
    }

    /** The identity key of an existing owner, derived from its stored fields. */
    public static String of(Owner owner) {
        return of(owner.getTelephone(), owner.getEmail(), owner.getLastName());
    }

    /** The identity key for the given normalized telephone, email and last name. */
    public static String of(String telephone, String email, String lastName) {
        String raw = part(telephone) + "|" + lowerCased(email) + "|" + Soundex.of(lastName);
        return Sha256.hexLower(raw);
    }

    /** A field value, or the empty string when absent. */
    private static String part(String value) {
        return value == null ? "" : value;
    }

    /** An email lower-cased, or the empty string when absent. */
    private static String lowerCased(String email) {
        return email == null ? "" : email.toLowerCase(Locale.ROOT);
    }
}
