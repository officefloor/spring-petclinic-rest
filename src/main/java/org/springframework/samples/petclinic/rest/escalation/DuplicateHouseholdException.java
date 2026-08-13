package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when another owner already has the same last name and the same address
 * (compared case-insensitively with collapsed whitespace) and the request did not opt in via
 * {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler}, which responds
 * 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", " + address);
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
