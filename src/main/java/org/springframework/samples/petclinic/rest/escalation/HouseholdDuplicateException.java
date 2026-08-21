package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join an existing household - it shares another owner's
 * computed {@code householdId} (the same last name and postcode) - without declaring
 * {@code sharesHousehold}. The household is now keyed on (lastName, postcode), so a second such
 * owner is a household duplicate rather than a mere soft match. Setting {@code sharesHousehold}
 * bypasses this block and creates the owner as a declared household member. Handled globally by
 * {@link HouseholdDuplicateExceptionHandler}, which responds 409.
 */
public class HouseholdDuplicateException extends Exception {

    private final String householdId;

    public HouseholdDuplicateException(String householdId) {
        super("Owner household already exists: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
