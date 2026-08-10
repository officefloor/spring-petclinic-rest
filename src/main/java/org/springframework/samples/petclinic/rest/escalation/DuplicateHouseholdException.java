package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would add an owner whose last name and address (compared
 * case-insensitively with collapsed whitespace) match an existing owner's — i.e. they share a
 * household — and the request did not opt in with {@code sharesHousehold: true}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner named '" + lastName + "' at address '" + address + "' already exists");
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
