package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by
 * {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueHousehold}
 * when a create-owner request would join an existing owner's household — the same computed
 * {@code householdId} (derived from last name and postcode) — without declaring
 * {@code sharesHousehold: true}. Handled by {@link HouseholdDuplicateExceptionHandler},
 * which responds 409.
 */
public class HouseholdDuplicateException extends Exception {

    public HouseholdDuplicateException(String householdId) {
        super("Owner household already registered: " + householdId);
    }
}
