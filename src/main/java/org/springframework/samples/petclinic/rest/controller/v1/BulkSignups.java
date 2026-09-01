package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Collection;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Bulk-signup rule for owners: a create is flagged once more than 80 owners have
 * already been created on the candidate's registration date.
 */
final class BulkSignups {

    private BulkSignups() {
    }

    /** Whether more than 80 existing owners share {@code candidate}'s registration date. */
    static boolean exceededDailyLimit(Collection<Owner> existing, Owner candidate) {
        return existing.stream()
            .filter(other -> candidate.getRegistrationDate().equals(other.getRegistrationDate()))
            .count() > 80;
    }
}
