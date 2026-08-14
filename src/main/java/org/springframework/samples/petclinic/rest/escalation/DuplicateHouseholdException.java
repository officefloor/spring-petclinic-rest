package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueHousehold} when a create-owner request carries a last name and
 * address that (compared case-insensitively with collapsed whitespace) already belong to another
 * owner. Handled globally by {@link DuplicateHouseholdExceptionHandler}, which responds 409. A
 * request that sets {@code sharesHousehold} true bypasses the check and is never rejected here.
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
