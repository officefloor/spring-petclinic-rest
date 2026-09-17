package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would place a new owner in a household (same last name and address) that
 * an existing owner already occupies, without acknowledging it via {@code sharesHousehold}. Carries the
 * offending last name and address so {@link DuplicateHouseholdExceptionHandler} can report them.
 * Handled as a 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("Another owner already shares this household: " + lastName + ", " + address);
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
