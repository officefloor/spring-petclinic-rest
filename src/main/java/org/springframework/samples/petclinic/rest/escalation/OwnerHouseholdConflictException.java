package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose last name and address (compared case-insensitively with
 * collapsed whitespace) already belong to another owner, and the request did not opt in with
 * {@code sharesHousehold: true}. Handled globally by {@link OwnerHouseholdConflictExceptionHandler},
 * which responds 409.
 */
public class OwnerHouseholdConflictException extends Exception {

    public OwnerHouseholdConflictException(String message) {
        super(message);
    }
}
