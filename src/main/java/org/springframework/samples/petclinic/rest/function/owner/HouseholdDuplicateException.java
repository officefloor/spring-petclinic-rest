package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueHousehold} when a create request would join an existing owner's
 * household (same last name and postcode) without declaring {@code sharesHousehold}. Carries the
 * colliding household id for the handler's message. Checked so it appears in the function's
 * {@code throws} clause and routes to an escalation.
 */
public class HouseholdDuplicateException extends Exception {

    private final String householdId;

    public HouseholdDuplicateException(String householdId) {
        super("Household already exists: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return householdId;
    }
}
