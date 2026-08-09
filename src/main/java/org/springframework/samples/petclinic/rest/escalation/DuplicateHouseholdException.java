package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose last name and address match an existing owner (compared
 * case-insensitively with collapsed whitespace) and the request did not acknowledge the shared
 * household. Handled globally by {@link DuplicateHouseholdExceptionHandler}, which responds 409
 * (Conflict).
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' already lives at '" + address
                + "'; set 'sharesHousehold' true to allow another owner at the same household");
    }
}
