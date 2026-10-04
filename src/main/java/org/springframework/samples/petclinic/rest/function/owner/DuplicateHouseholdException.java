package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerHouseholdUnique} when an owner create request shares both a last
 * name and an address (compared case-insensitively with collapsed whitespace) with an existing
 * owner, and the request did not opt in via {@code sharesHousehold}. Checked so it appears in the
 * {@code throws} clause and is routed to its escalation handler, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", "
                + address);
    }
}
