package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft duplicate detection. A candidate that is not a hard {@link IdentityKey} duplicate but shares
 * an existing owner's household (same computed household id) while using a different telephone is a
 * possible duplicate of that owner. A declared household member ({@code sharesHousehold}) is not a
 * suspected duplicate.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** Id of the earliest existing owner {@code candidate} softly duplicates, or {@code null} when none. */
    static Integer matchId(Owner candidate, Collection<Owner> existing, Boolean sharesHousehold) {
        if (Boolean.TRUE.equals(sharesHousehold) || candidate.getHouseholdId() == null) {
            return null;
        }
        String id = candidate.getHouseholdId();
        return existing.stream()
            .filter(other -> id.equals(other.getHouseholdId()))
            .filter(other -> !candidate.getTelephone().equals(other.getTelephone()))
            .map(Owner::getId)
            .filter(other -> other != null)
            .min(Integer::compareTo)
            .orElse(null);
    }
}
