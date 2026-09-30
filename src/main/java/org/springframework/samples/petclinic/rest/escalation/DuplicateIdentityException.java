package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerIdentityUnique} when a new owner's whole identity key
 * ({@code normalizedTelephone|email|householdId}) equals an existing owner's. This single check
 * consolidates the former separate telephone, email and household duplicate checks. Handled
 * globally by {@link DuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
