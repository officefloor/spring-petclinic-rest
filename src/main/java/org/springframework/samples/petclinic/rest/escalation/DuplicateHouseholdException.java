package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same last name and address as an existing owner
 * (a shared household) without opting in via {@code sharesHousehold}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 with the conflicting values.
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
        return this.lastName;
    }

    public String getAddress() {
        return this.address;
    }
}
