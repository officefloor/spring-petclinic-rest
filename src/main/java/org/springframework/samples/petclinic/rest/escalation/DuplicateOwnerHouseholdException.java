package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's last name and address match an existing
 * owner (compared case-insensitively with collapsed whitespace), i.e. a duplicate household, and
 * the request did not set {@code sharesHousehold} true. Handled globally by
 * {@link DuplicateOwnerHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("An owner named " + lastName + " already exists at address " + address);
    }
}
