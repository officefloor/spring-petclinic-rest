package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose household — the deterministic {@code householdId}
 * derived from {@code (lastName, postcode)} — already belongs to another owner. Because the
 * household is keyed on {@code (lastName, postcode)}, a second owner sharing an existing
 * owner's lastName and postcode is a household duplicate. Setting {@code sharesHousehold}
 * bypasses this check (the new owner is created as a declared household member). Handled
 * globally by {@link DuplicateHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    private final String householdId;

    public DuplicateHouseholdException(String householdId) {
        super("An owner in household " + householdId + " already exists");
        this.householdId = householdId;
    }

    /** The householdId that is already in use. */
    public String getHouseholdId() {
        return this.householdId;
    }
}
