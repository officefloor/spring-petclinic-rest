package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when another owner already shares the request's
 * lastName and address (compared case-insensitively with collapsed whitespace) and the
 * request did not opt in with {@code sharesHousehold=true}. Carries the offending lastName
 * so {@link DuplicateOwnerHouseholdExceptionHandler} can report it. Handled as 409 Conflict.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    private final String lastName;

    public DuplicateOwnerHouseholdException(String lastName) {
        super("Owner household already in use: " + lastName);
        this.lastName = lastName;
    }

    public String getLastName() {
        return this.lastName;
    }
}
