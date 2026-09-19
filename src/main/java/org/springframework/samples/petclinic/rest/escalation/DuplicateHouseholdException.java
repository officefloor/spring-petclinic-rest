package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would repeat an existing owner in a household (same
 * last name and postcode, so the same derived {@code householdId}, and the same telephone)
 * without opting in to sharing that household. A different person in the same household is a
 * legitimate additional member and is not rejected here. Handled by
 * {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String householdId;

    public DuplicateHouseholdException(String householdId) {
        super("An owner already exists in this household: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
