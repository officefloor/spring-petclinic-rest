package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies the same last name and address (compared
 * case-insensitively with collapsed whitespace) as an existing owner, and the request did
 * not opt in with {@code sharesHousehold}. Handled as a 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with last name '" + lastName + "' already exists at address '" + address
                + "'; set sharesHousehold to allow sharing a household");
    }
}
