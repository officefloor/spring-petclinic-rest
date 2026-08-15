package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a lastName and address that together already
 * identify an existing owner's household (compared case-insensitively with collapsed
 * whitespace), and the request did not opt in with {@code sharesHousehold=true}. Handled
 * globally by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same lastName and address already exists: " + lastName + ", " + address);
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
