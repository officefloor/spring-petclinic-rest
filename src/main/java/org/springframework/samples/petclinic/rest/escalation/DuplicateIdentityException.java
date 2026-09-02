package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectDuplicateIdentity} when a create request's whole identityKey is already
 * used by another owner. Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner already in use: " + identityKey);
    }
}
