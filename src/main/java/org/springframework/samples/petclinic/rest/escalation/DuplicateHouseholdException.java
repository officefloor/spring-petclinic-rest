package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a last name and address whose normalized
 * (case-insensitive, whitespace-collapsed) form is already shared by an existing owner,
 * and the request did not opt in with {@code sharesHousehold}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' at address '" + address
                + "' already exists");
    }
}
