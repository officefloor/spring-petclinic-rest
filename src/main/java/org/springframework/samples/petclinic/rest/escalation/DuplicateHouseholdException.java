package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner has the same last name and address (compared
 * case-insensitively with collapsed whitespace) as an owner that already exists, i.e. another member
 * of the same household, without opting in via {@code sharesHousehold}. Carries the offending last name
 * and address so {@link DuplicateHouseholdExceptionHandler} can report them. Checked so it appears in
 * the function's {@code throws} clause and is routed as an escalation (a 409 Conflict).
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner named '" + lastName + "' already exists at address '" + address + "'");
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
