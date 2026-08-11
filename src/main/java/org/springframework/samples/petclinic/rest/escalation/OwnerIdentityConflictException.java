package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose whole identityKey
 * ({@code normalizedTelephone|email|householdId}) is already used by another owner. This single
 * check consolidates the former separate telephone, email and household duplicate checks. Handled
 * globally by {@link OwnerIdentityConflictExceptionHandler}, which responds 409.
 */
public class OwnerIdentityConflictException extends Exception {

    public OwnerIdentityConflictException(String message) {
        super(message);
    }
}
