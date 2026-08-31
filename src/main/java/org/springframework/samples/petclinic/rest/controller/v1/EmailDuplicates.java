package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Detects whether a candidate owner's email is already used by another owner,
 * compared case-insensitively.
 */
final class EmailDuplicates {

    private EmailDuplicates() {
    }

    /** Lower-case a value for comparison, treating null as empty. */
    private static String normalize(String value) {
        return value == null ? "" : value.toLowerCase();
    }

    /** Whether an existing owner already uses the candidate's email. */
    static boolean isDuplicate(Collection<Owner> existing, Owner candidate) {
        String email = normalize(candidate.getEmail());
        if (email.isEmpty()) {
            return false;
        }
        return existing.stream().anyMatch(o -> normalize(o.getEmail()).equals(email));
    }
}
