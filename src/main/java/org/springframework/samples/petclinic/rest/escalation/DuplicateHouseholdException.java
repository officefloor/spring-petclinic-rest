package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would place an owner in a household (same lastName and address,
 * compared case-insensitively with collapsed whitespace) that an existing owner already occupies, and
 * the request did not opt in with {@code sharesHousehold: true}. Handled globally by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name already lives at this address: " + lastName + ", " + address);
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
