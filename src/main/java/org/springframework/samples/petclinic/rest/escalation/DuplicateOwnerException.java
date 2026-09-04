package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request belongs to the same household as an existing owner — that is,
 * it shares the deterministic {@code householdId} derived from the normalized last name and postcode —
 * and did not opt into the shared household via {@code sharesHousehold}. Handled by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String householdId) {
        super("An owner in the same household already exists: " + householdId);
    }
}
