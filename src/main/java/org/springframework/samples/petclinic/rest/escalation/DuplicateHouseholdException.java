package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when another owner already shares the same last name
 * at the same address (compared case-insensitively with collapsed whitespace), i.e. the same
 * household, and the request did not opt in with {@code sharesHousehold = true}.
 *
 * <p>Handled by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
