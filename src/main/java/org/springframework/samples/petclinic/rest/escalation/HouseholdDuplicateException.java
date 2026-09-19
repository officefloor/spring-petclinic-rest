package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join an existing household (an existing owner shares
 * the same household id, i.e. the same last name and postcode) without declaring it. Since a
 * household is keyed on (last name, postcode), such an owner is a household duplicate. Handled by
 * {@link HouseholdDuplicateExceptionHandler}, which responds 409 with the conflicting household
 * id and the existing owner's id. A request that sets {@code sharesHousehold} bypasses this block
 * and is created as a declared household member instead.
 */
public class HouseholdDuplicateException extends Exception {

    private final String householdId;

    private final Integer existingOwnerId;

    public HouseholdDuplicateException(String householdId, Integer existingOwnerId) {
        super("Owner shares a household with an existing owner: " + householdId);
        this.householdId = householdId;
        this.existingOwnerId = existingOwnerId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }

    public Integer getExistingOwnerId() {
        return this.existingOwnerId;
    }
}
