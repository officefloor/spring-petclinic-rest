package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would join an existing household (same {@code householdId},
 * derived from lastName + postcode) without declaring {@code sharesHousehold}. Handled by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String householdId) {
        super("An owner in household '" + householdId + "' already exists");
    }
}
