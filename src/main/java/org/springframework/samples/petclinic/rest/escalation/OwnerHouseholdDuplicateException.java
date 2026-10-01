package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerHouseholdUnique}
 * when the owner being created has the same lastName and address (compared case-insensitively with
 * collapsed whitespace) as an existing owner and the request did not set {@code sharesHousehold} true.
 * Handled by {@link OwnerHouseholdDuplicateExceptionHandler}, which responds 409.
 */
public class OwnerHouseholdDuplicateException extends Exception {

    private final String lastName;

    private final String address;

    public OwnerHouseholdDuplicateException(String lastName, String address) {
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
