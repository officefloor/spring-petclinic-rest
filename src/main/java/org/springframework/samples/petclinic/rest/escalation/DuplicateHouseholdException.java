package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireUniqueHousehold} when a create-owner request would create a second owner
 * sharing a household with an existing one — same last name and same address, compared
 * case-insensitively with collapsed whitespace — and the request did not set {@code sharesHousehold}
 * true. {@link DuplicateHouseholdExceptionHandler} reports a 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: '" + lastName + "', '"
                + address + "'");
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
