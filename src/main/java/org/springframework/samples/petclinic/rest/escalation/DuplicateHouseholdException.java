package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request has the same last name and address as an existing owner and does
 * not opt into sharing a household. Handled by {@link DuplicateHouseholdExceptionHandler}, which
 * responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Owner already exists at this address: " + lastName + ", " + address);
    }
}
