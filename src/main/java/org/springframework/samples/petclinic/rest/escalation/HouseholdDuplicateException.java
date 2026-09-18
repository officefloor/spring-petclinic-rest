package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request would join a household that already has a member — i.e. an
 * existing owner shares its deterministic household id (same last name and postcode) — without
 * opting in via {@code sharesHousehold}. Handled by {@link HouseholdDuplicateExceptionHandler}, which
 * responds 409 Conflict.
 */
public class HouseholdDuplicateException extends Exception {

    public HouseholdDuplicateException(String householdId) {
        super("Household already has a member: " + householdId);
    }
}
