package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join a household — the same last name and
 * address — already occupied by another owner, without opting in via
 * {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler} as a 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already in use: " + lastName + ", " + address);
    }
}
