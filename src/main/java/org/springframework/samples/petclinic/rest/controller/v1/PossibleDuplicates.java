package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects a "soft" duplicate: an owner that is not a hard duplicate but shares an
 * existing owner's last name (compared case-insensitively) and postcode while carrying
 * a different telephone. The different telephone excludes the owner from matching itself.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** Lower-case a value for comparison, treating null as empty. */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    /** The id of the earliest existing owner softly matching the candidate, or null when none. */
    static Integer matchId(Collection<Owner> existing, Owner candidate) {
        if (candidate.getPostcode() == null) {
            return null;
        }
        String lastName = normalize(candidate.getLastName());
        return existing.stream()
            .filter(o -> normalize(o.getLastName()).equals(lastName)
                && candidate.getPostcode().equals(o.getPostcode())
                && !candidate.getTelephone().equals(o.getTelephone()))
            .map(Owner::getId)
            .min(Integer::compareTo)
            .orElse(null);
    }
}
