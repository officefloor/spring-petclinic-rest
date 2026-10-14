package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when another owner already has the same last name and address, and the request does not
 * declare that the new owner shares that household.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists");
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
