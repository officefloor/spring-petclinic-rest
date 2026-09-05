package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request has the same last name and address (compared
 * case-insensitively with collapsed whitespace) as an existing owner and does not opt
 * in with {@code sharesHousehold}. Handled by {@link DuplicateHouseholdHandler}, which
 * responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
