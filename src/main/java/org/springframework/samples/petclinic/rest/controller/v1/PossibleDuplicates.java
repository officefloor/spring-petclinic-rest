package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft duplicate detection. A candidate that is not a hard {@link IdentityKey} duplicate but whose
 * last name (by {@link Soundex}) and postcode match an existing owner's is a possible duplicate of
 * that owner. A declared household member ({@code sharesHousehold}) is not a suspected duplicate.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** Id of the earliest existing owner {@code candidate} softly duplicates, or {@code null} when none. */
    static Integer matchId(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold)) {
            return null;
        }
        String key = IdentityKey.of(candidate);
        String soundex = Soundex.code(candidate.getLastName());
        String postcode = candidate.getPostcode();
        return existing.stream()
            .filter(other -> !IdentityKey.of(other).equals(key))
            .filter(other -> soundex.equals(Soundex.code(other.getLastName())))
            .filter(other -> postcode != null && postcode.equals(other.getPostcode()))
            .map(Owner::getId)
            .filter(other -> other != null)
            .min(Integer::compareTo)
            .orElse(null);
    }
}
