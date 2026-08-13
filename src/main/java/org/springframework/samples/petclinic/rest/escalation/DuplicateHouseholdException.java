package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueHousehold}
 * when a create-owner request shares a household — the same last name and the same
 * address (compared case-insensitively with collapsed whitespace) — with an existing
 * owner, and the request did not opt in via {@code sharesHousehold: true}. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String lastName, String address) {
        super("Household already in use: " + lastName + " at " + address);
    }
}
