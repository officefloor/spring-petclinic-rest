package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request shares another owner's household (same last name and address).
 * Handled by {@link DuplicateOwnerHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Household already in use: " + lastName + ", " + address);
    }
}
