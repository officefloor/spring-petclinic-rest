package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would place a new owner in a household (same last
 * name and address) already occupied by another owner, and the request did not opt in
 * with {@code sharesHousehold}. Handled by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with this last name and address already exists: " + lastName + ", " + address);
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
