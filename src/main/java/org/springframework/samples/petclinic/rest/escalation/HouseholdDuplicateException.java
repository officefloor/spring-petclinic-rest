package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner resolves to the same {@code householdId} (derived from the
 * normalized last name and the postcode) as an existing owner — i.e. the two owners are the same
 * household. Carries the offending household id so {@link HouseholdDuplicateExceptionHandler} can report
 * it. Setting {@code sharesHousehold} on the request bypasses this block (the owner is admitted as a
 * declared household member). Checked so it appears in the function's {@code throws} clause and is routed
 * as an escalation (a 409 Conflict).
 */
public class HouseholdDuplicateException extends Exception {

    private final String householdId;

    public HouseholdDuplicateException(String householdId) {
        super("An owner in household '" + householdId + "' already exists");
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
