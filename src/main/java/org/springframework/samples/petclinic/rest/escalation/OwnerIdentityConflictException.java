package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request would add a second owner to an existing household — i.e. an
 * owner whose deterministic {@code householdId} (derived from its last name and postcode)
 * equals that of an existing owner — without opting in via {@code sharesHousehold}. Handled
 * by {@link OwnerIdentityConflictExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerIdentityConflictException extends Exception {

    private final String householdId;

    public OwnerIdentityConflictException(String householdId) {
        super("Another owner already exists in the same household: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return householdId;
    }
}
