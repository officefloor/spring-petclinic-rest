package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's whole derived {@code identityKey}
 * (normalizedTelephone + '|' + email + '|' + householdId) already identifies another owner.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
