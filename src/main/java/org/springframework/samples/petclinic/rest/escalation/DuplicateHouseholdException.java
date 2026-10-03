package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when another owner already shares the household of the
 * owner being created: the same last name and the same address (compared case-insensitively
 * with collapsed whitespace). Handled globally by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409. Suppressed when the request sets {@code sharesHousehold} true.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
