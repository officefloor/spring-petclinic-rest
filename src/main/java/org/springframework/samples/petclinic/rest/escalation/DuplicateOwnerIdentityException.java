package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's {@code identityKey} is already used by another owner.
 * Handled by {@link DuplicateOwnerIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
    }
}
