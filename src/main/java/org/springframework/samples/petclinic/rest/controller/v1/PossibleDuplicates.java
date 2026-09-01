package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Soft-match rule for owners: a candidate is a possible duplicate of an existing owner
 * when they share the same last name (case-insensitive) and postcode but have a
 * different telephone, so it is created rather than rejected as a hard duplicate.
 */
final class PossibleDuplicates {

    private PossibleDuplicates() {
    }

    /** The id of an existing owner {@code candidate} softly matches, or {@code null} if none. */
    static Integer matchId(Collection<Owner> existing, Owner candidate) {
        return existing.stream()
            .filter(other -> softMatch(other, candidate))
            .map(Owner::getId)
            .findFirst()
            .orElse(null);
    }

    private static boolean softMatch(Owner a, Owner b) {
        return b.getPostcode() != null && b.getPostcode().equals(a.getPostcode())
            && a.getLastName() != null && a.getLastName().equalsIgnoreCase(b.getLastName())
            && a.getTelephone() != null && !a.getTelephone().equals(b.getTelephone());
    }
}
