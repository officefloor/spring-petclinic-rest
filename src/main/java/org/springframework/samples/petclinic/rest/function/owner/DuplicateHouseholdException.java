package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDuplicateHousehold} when a create-owner request has the same last name and
 * address (compared case-insensitively with collapsed whitespace) as an existing owner, and the
 * request did not set {@code sharesHousehold} true. Handled globally by
 * {@code DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' already lives at address '" + address
                + "'; set sharesHousehold true to allow.");
    }
}
