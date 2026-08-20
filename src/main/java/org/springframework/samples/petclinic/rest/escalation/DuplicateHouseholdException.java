package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose last name and address match an existing owner's
 * (compared case-insensitively with collapsed whitespace), unless the request opts in via
 * {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner named " + lastName + " already lives at " + address);
    }
}
