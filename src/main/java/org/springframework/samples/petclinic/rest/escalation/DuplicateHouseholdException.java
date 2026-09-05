package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireUniqueHousehold}
 * when a create-owner request has the same lastName and address (compared case-insensitively
 * with collapsed whitespace) as an existing owner, and the request did not opt into a shared
 * household. Handled by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already exists for last name '" + lastName + "' at address '" + address + "'");
    }
}
