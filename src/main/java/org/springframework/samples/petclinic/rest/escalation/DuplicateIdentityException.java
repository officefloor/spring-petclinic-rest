package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request is a household duplicate: an existing owner already shares the new
 * owner's household id (same last name and postcode) and the request did not set
 * {@code sharesHousehold}. Carries the offending household id so
 * {@link DuplicateIdentityExceptionHandler} can report it. Handled as a 409.
 */
public class DuplicateIdentityException extends Exception {

    private final String householdId;

    public DuplicateIdentityException(String householdId) {
        super("Another owner already shares this household: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return householdId;
    }
}
