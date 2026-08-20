package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerHouseholdUnique} when a create-owner request shares a
 * household with an existing owner — the same last name and the same address, compared
 * case-insensitively with collapsed whitespace — and the request did not opt in with
 * {@code sharesHousehold}. Handled by {@link DuplicateOwnerHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Household is already registered to another owner: " + lastName + ", " + address);
    }
}
