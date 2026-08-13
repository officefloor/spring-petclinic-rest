package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueIdentity}
 * when a create-owner request's whole derived {@code identityKey}
 * ({@code normalizedTelephone|email|householdId}) already belongs to an existing owner.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
