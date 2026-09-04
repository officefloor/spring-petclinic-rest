package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request shares a last name and address with an existing
 * owner and does not opt in via {@code sharesHousehold}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Owner already exists at this household: " + lastName + ", " + address);
    }
}
