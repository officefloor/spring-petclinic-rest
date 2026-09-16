package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request shares a household (same last name and address,
 * compared case-insensitively with collapsed whitespace) with an existing owner and does
 * not opt in via {@code sharesHousehold}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: "
                + lastName + ", " + address);
    }
}
