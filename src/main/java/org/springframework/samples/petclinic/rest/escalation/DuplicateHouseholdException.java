package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when another owner already has the same last name and address
 * (compared case-insensitively with collapsed whitespace) and the request did not opt in with
 * {@code sharesHousehold=true}. Handled globally by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
