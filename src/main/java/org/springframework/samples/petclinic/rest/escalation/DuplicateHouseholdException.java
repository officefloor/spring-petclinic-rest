package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would be a second owner in an existing household
 * (same last name and postcode, so the same derived {@code householdId}) without opting in
 * to sharing that household. Handled by {@link DuplicateHouseholdExceptionHandler}, which
 * responds 409.
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
