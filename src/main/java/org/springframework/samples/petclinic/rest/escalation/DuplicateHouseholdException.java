package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckHouseholdUnique}
 * when a new owner shares both last name and address (compared case-insensitively with collapsed
 * whitespace) with an existing owner, and the request did not opt in via {@code sharesHousehold}.
 * Handled globally by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name already lives at this address: "
                + lastName + " / " + address);
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
