package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueOwnerHousehold} when a new owner shares its last name and
 * address with an existing owner. Handled globally by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
