package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lastName and address (compared
 * case-insensitively with collapsed whitespace) already belong to another owner
 * and the request did not opt in via {@code sharesHousehold}. Handled globally by
 * {@link DuplicateOwnerHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String message) {
        super(message);
    }
}
