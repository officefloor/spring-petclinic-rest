package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request is a household duplicate: an existing owner shares the same
 * deterministic household id (derived from the normalized last name and postcode), and the request
 * did not opt in with {@code sharesHousehold}. This is the single duplicate condition. Handled as a
 * 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String householdId) {
        super("An owner in the same household already exists (householdId '" + householdId + "')");
    }
}
