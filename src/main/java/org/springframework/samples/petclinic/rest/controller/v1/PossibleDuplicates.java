package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft duplicate detection. A candidate that is not a hard {@link IdentityKey} duplicate
 * but shares an existing owner's lastName (case-insensitively) and postcode while using a
 * different telephone is a possible duplicate of that owner.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** Id of the earliest existing owner {@code candidate} softly duplicates, or {@code null} when none. */
    static Integer matchId(Owner candidate, Collection<Owner> existing) {
        if (candidate.getPostcode() == null) {
            return null;
        }
        return existing.stream()
            .filter(other -> candidate.getPostcode().equals(other.getPostcode()))
            .filter(other -> candidate.getLastName() != null && candidate.getLastName().equalsIgnoreCase(other.getLastName()))
            .filter(other -> !candidate.getTelephone().equals(other.getTelephone()))
            .map(Owner::getId)
            .filter(id -> id != null)
            .min(Integer::compareTo)
            .orElse(null);
    }
}
