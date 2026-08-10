package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckUniqueHousehold} when a create-owner request has the same last name and
 * the same address (compared case-insensitively with collapsed whitespace) as an existing owner,
 * and the request did not opt in with {@code sharesHousehold=true}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' already lives at address '" + address
                + "'; set sharesHousehold=true to allow a shared household.");
    }
}
