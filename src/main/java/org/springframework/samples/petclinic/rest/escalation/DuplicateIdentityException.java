package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request targets a {@code (lastName, postcode)} household —
 * identified by its {@code householdId} — that an existing owner already occupies, i.e. a
 * household duplicate. Handled by {@link DuplicateIdentityExceptionHandler}, which responds
 * 409. A request that opts in with {@code sharesHousehold} is a declared member and does
 * not trigger this.
 */
public class DuplicateIdentityException extends Exception {

    private final String householdId;

    public DuplicateIdentityException(String householdId) {
        super("Household already registered: " + householdId);
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return this.householdId;
    }
}
