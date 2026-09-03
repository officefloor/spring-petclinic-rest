package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lastName and address match another owner's
 * (case-insensitively, whitespace collapsed). Handled by
 * {@link DuplicateHouseholdHandler}, which responds 409, unless the request opts in
 * with {@code sharesHousehold=true}.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already exists for: " + lastName + ", " + address);
    }
}
