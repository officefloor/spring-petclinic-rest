package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join an existing household (same last name and
 * postcode) without opting in via {@code sharesHousehold}. Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String householdId) {
        super("An owner in household " + householdId + " already exists");
    }
}
