package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckHouseholdUnique}
 * when a create-owner request carries a last name and address already used together by another
 * owner and the request did not opt in via {@code sharesHousehold}. Handled globally by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already in use: " + lastName + " at " + address);
    }
}
