package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerHouseholdUnique} when a new owner shares both lastName and
 * address (compared case-insensitively with collapsed whitespace) with an existing owner
 * and the request did not opt in via {@code sharesHousehold=true}. Handled globally by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
