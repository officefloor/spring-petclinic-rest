package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner "possible duplicate": a softer match than the hard
 * {@link IdentityKey}. A new owner possibly duplicates an existing one when they share the
 * same last name (compared case-insensitively) and the same postcode but have
 * <em>different</em> telephones — enough overlap to look like the same person, yet distinct
 * enough that {@link EnsureUniqueIdentity} did not treat it as a hard duplicate.
 *
 * <p>The postcode must be present on both sides: two owners with no postcode do not share
 * one, so they are not flagged.
 *
 * @see FlagPossibleDuplicate records the matching owner at registration.
 */
final class PossibleDuplicate {

    private PossibleDuplicate() {
    }

    /** Whether {@code existing} is a possible duplicate of the new {@code candidate}. */
    static boolean matches(Owner existing, Owner candidate) {
        return sameLastName(existing, candidate)
                && samePostcode(existing, candidate)
                && differentTelephone(existing, candidate);
    }

    private static boolean sameLastName(Owner existing, Owner candidate) {
        return normalize(existing.getLastName()).equals(normalize(candidate.getLastName()));
    }

    private static boolean samePostcode(Owner existing, Owner candidate) {
        String postcode = candidate.getPostcode();
        return postcode != null && !postcode.isBlank() && postcode.equals(existing.getPostcode());
    }

    private static boolean differentTelephone(Owner existing, Owner candidate) {
        return !normalize(candidate.getTelephone()).equals(normalize(existing.getTelephone()));
    }

    /** A value trimmed and lower-cased, so comparison is case-insensitive. */
    private static String normalize(String value) {
        return value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
    }
}
