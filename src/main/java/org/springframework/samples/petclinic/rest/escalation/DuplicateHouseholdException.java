package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request shares a last name and address with an existing owner
 * (compared case-insensitively, with runs of whitespace collapsed) and the request did not
 * opt in with {@code sharesHousehold=true}. Carries the normalized last name and address so
 * the handler can report what collided.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name and address already exists: '" + lastName
                + "' at '" + address + "'");
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
