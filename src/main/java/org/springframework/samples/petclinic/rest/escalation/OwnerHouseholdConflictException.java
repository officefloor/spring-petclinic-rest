package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would add an owner whose last name and address match
 * those of an existing owner (compared case-insensitively with collapsed whitespace)
 * and the request has not opted in via {@code sharesHousehold}. Handled by
 * {@link OwnerHouseholdConflictExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerHouseholdConflictException extends Exception {

    private final String lastName;

    private final String address;

    public OwnerHouseholdConflictException(String lastName, String address) {
        super("Another owner already lives at this address: " + lastName + ", " + address);
        this.lastName = lastName;
        this.address = address;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAddress() {
        return address;
    }
}
