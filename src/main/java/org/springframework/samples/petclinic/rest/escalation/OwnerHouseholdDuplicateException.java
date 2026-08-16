package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when another owner already has the same
 * {@code lastName} and {@code address} (compared case-insensitively with collapsed
 * whitespace) and the request did not set {@code sharesHousehold} true.
 *
 * <p>Carries the offending last name and address so
 * {@link OwnerHouseholdDuplicateExceptionHandler} can respond 409 explaining why the
 * create was rejected.
 */
public class OwnerHouseholdDuplicateException extends Exception {

    private final String lastName;

    private final String address;

    public OwnerHouseholdDuplicateException(String lastName, String address) {
        super("An owner named '" + lastName + "' already lives at '" + address + "'");
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
