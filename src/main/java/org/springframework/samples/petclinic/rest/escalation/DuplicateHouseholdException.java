package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerHouseholdUnique} when a new owner would join an existing household —
 * an existing owner shares the new owner's computed {@code householdId} (same lastName and
 * postcode) — and the request did not opt in with {@code sharesHousehold}. Handled globally by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String message) {
        super(message);
    }
}
