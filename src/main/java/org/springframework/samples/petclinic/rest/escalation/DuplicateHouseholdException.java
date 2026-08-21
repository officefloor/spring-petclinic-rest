package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request shares both last name and address with an existing owner (the
 * two compared case-insensitively with collapsed whitespace) and the request did not opt in with
 * {@code sharesHousehold: true}. Handled globally by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already in use: " + lastName + ", " + address);
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
