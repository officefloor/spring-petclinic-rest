package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same last name and address as an existing owner and
 * does not opt in with {@code sharesHousehold}. Handled by
 * {@link DuplicateOwnerHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Owner already exists at this household: " + lastName + ", " + address);
    }
}
