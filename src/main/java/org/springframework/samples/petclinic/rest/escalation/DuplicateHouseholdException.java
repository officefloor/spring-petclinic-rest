package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same last name and address as an existing
 * owner (compared case-insensitively with collapsed whitespace) and the request did not
 * opt in with {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("Owner already exists at this household: " + lastName + ", " + address);
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
