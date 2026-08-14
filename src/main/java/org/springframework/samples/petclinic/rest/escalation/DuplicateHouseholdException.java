package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueHousehold} when a create-owner request carries a lastName and address
 * already used together by another owner (compared case-insensitively with collapsed whitespace) and
 * the request did not set {@code sharesHousehold} true. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already registered for lastName '" + lastName + "' at address '" + address + "'");
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
