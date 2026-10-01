package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerHouseholdUnique} when a create request carries a last name and address
 * that, normalized, already belong to another owner and the request did not opt in with
 * {@code sharesHousehold: true}. Handled globally by {@code OwnerHouseholdDuplicateExceptionHandler},
 * which responds 409 Conflict.
 */
public class OwnerHouseholdDuplicateException extends Exception {

    private final String lastName;

    private final String address;

    public OwnerHouseholdDuplicateException(String lastName, String address) {
        super("An owner with the same last name and address already exists: " + lastName + ", "
                + address);
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
