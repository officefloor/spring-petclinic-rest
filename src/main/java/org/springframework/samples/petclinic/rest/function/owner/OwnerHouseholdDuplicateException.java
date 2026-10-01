package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerHouseholdUnique} when a create request would be a second owner in an
 * existing household (same deterministic {@code householdId}). Handled globally by
 * {@code OwnerHouseholdDuplicateExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerHouseholdDuplicateException extends Exception {

    private final String householdId;

    public OwnerHouseholdDuplicateException(String householdId) {
        super("Owner household already in use: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return householdId;
    }
}
