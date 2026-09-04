package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same last name and address as an existing owner
 * (compared case-insensitively with collapsed whitespace) and does not opt in via
 * {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", "
                + address);
    }
}
