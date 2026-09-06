package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.EnsureUniqueIdentity}
 * when the whole {@code identityKey} of an owner being created is already used by another owner.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
