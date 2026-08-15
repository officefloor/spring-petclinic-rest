package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckIdentityUnique}
 * when a create-owner request's whole derived {@code identityKey}
 * ({@code normalizedTelephone|email|householdId}) already belongs to another owner. Handled globally
 * by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
