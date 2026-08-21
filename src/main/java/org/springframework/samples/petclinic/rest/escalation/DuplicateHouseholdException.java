package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a lastName and address already used together by
 * another owner (compared case-insensitively with collapsed whitespace) and the request
 * does not opt in via {@code sharesHousehold}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with this last name already lives at this address: " + lastName + ", " + address);
    }
}
