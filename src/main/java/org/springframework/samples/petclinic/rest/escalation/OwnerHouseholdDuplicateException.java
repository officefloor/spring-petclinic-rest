package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by
 * {@link org.springframework.samples.petclinic.rest.function.owner.CheckOwnerHouseholdUnique} when the
 * owner being created would be a second member of an existing household — it shares an existing owner's
 * {@code householdId} (derived from {@code lastName} and {@code postcode}) — without declaring that it
 * shares the household via {@code sharesHousehold}. Handled by
 * {@link OwnerHouseholdDuplicateExceptionHandler}, which responds 409.
 */
public class OwnerHouseholdDuplicateException extends Exception {

    private final String householdId;

    public OwnerHouseholdDuplicateException(String householdId) {
        super("Owner belongs to an existing household: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
