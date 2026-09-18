package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would place a second owner in a household (same last name and
 * address, compared case-insensitively with collapsed whitespace) that an existing owner already
 * occupies, and the request did not opt in with {@code sharesHousehold=true}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", " + address);
    }
}
