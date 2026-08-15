package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when a request would be a second owner in an existing
 * household — i.e. an owner with the same {@code householdId} (same last name and postcode) already
 * exists — and the request does not declare {@code sharesHousehold}. Handled globally by {@link
 * DuplicateOwnerIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String householdId) {
        super("An owner in household " + householdId + " already exists");
    }
}
