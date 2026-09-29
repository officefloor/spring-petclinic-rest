package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lastName and address (compared case-insensitively
 * with collapsed whitespace) already belong to another owner, unless the request opts in
 * with {@code sharesHousehold} true. Handled globally by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner named " + lastName + " already lives at " + address);
        this.lastName = lastName;
        this.address = address;
    }

    /** The lastName that already shares the household. */
    public String getLastName() {
        return this.lastName;
    }

    /** The address that already shares the household. */
    public String getAddress() {
        return this.address;
    }
}
