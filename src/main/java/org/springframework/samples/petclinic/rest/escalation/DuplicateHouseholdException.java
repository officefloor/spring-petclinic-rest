package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request would add an owner who shares a household (same last name and
 * same address, compared case-insensitively with collapsed whitespace) with an existing owner, and
 * the request did not opt in with {@code sharesHousehold: true}. Checked so it appears in a
 * {@code throws} clause and can be routed to its escalation handler, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with this last name and address already exists: " + lastName + ", " + address);
        this.lastName = lastName;
        this.address = address;
    }

    /** The last name that is already in use at the address. */
    public String getLastName() {
        return this.lastName;
    }

    /** The address that already has an owner with the same last name. */
    public String getAddress() {
        return this.address;
    }
}
