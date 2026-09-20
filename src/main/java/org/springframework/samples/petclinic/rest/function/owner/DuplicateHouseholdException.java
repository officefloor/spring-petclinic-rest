package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueHousehold} when a create request would place a new owner in a
 * household (same last name and address) already occupied by an existing owner, unless the
 * request opted in with {@code sharesHousehold}. Carries the offending values for the
 * handler's message. Checked so it appears in the function's {@code throws} clause and routes
 * to an escalation.
 */
public class DuplicateHouseholdException extends Exception {

    private final String lastName;

    private final String address;

    public DuplicateHouseholdException(String lastName, String address) {
        super("An owner with the same last name already lives at this address: " + lastName
                + ", " + address);
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
