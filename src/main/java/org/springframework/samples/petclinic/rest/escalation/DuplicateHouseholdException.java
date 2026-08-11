package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would register an owner whose last name and address match
 * (case-insensitively, with collapsed whitespace) those of an existing owner, and the request did
 * not opt in with {@code sharesHousehold: true}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", "
                + address);
        this.lastName = lastName;
        this.address = address;
    }

    public String getLastName() {
        return this.lastName;
    }

    public String getAddress() {
        return this.address;
    }
}
