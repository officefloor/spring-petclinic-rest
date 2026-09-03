package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Enforces email uniqueness: an owner's email (compared case-insensitively / lower-cased)
 * may not already belong to any other owner.
 */
final class DuplicateEmails {

    private DuplicateEmails() {
    }

    /**
     * Returns {@code true} when {@code candidate}'s email is already used by one of the
     * {@code existing} owners, comparing lower-cased (case-insensitively). A {@code null}
     * candidate email is never a duplicate.
     */
    static boolean isTaken(Owner candidate, Collection<Owner> existing) {
        String email = candidate.getEmail();
        if (email == null) {
            return false;
        }
        return existing.stream()
            .map(Owner::getEmail)
            .filter(Objects::nonNull)
            .anyMatch(email::equalsIgnoreCase);
    }
}
