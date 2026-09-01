package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose last name and address already belong to another owner
 * (compared case-insensitively with collapsed whitespace). Handled by
 * {@link DuplicateHouseholdExceptionHandler} as 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already registered: " + lastName + " at " + address);
    }
}
