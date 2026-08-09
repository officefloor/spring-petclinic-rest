package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner that would join an existing household — another owner already shares
 * the deterministic {@code householdId} derived from {@code (normalizedLastName, postcode)} — without
 * the request acknowledging it via {@code sharesHousehold}. Setting {@code sharesHousehold} true
 * bypasses this block and creates the owner as a declared household member instead. Handled globally
 * by {@link DuplicateHouseholdExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String householdId) {
        super("An owner in household '" + householdId + "' already exists; "
                + "set sharesHousehold to add a declared household member");
    }
}
