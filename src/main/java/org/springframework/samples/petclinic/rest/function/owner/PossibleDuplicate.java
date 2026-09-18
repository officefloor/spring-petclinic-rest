package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The single definition of an owner "possible duplicate": a softer match than the hard
 * {@link IdentityKey}. A new owner possibly duplicates an existing one when their last names
 * share the same {@link Soundex} code and they share the same postcode, yet their
 * {@link IdentityKey identity keys} differ — enough overlap to look like the same person
 * (typically a different telephone or email), yet distinct enough that
 * {@link EnsureUniqueIdentity} did not treat it as a hard duplicate.
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
        return samePostcode(existing, candidate)
                && sameSoundexLastName(existing, candidate)
                && differentIdentity(existing, candidate);
    }

    private static boolean sameSoundexLastName(Owner existing, Owner candidate) {
        return Soundex.of(existing.getLastName()).equals(Soundex.of(candidate.getLastName()));
    }

    private static boolean samePostcode(Owner existing, Owner candidate) {
        String postcode = candidate.getPostcode();
        return postcode != null && !postcode.isBlank() && postcode.equals(existing.getPostcode());
    }

    private static boolean differentIdentity(Owner existing, Owner candidate) {
        return !IdentityKey.of(existing).equals(IdentityKey.of(candidate));
    }
}
