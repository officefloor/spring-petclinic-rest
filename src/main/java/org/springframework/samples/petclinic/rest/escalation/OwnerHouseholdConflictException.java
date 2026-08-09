package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would duplicate an existing owner's household — the same
 * lastName and the same address (compared case-insensitively with collapsed whitespace) — and the
 * request did not opt in via {@code sharesHousehold: true}. Handled globally by
 * {@link OwnerHouseholdConflictExceptionHandler}, which responds 409.
 */
public class OwnerHouseholdConflictException extends Exception {

    public OwnerHouseholdConflictException(String lastName, String address) {
        super("Another owner already lives at this household: " + lastName + ", " + address);
    }
}
