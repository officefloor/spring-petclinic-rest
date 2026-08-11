package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's whole {@code identityKey} already belongs to another owner.
 * Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    private final String identityKey;

    public DuplicateIdentityException(String identityKey) {
        super("Owner identity is already used by another owner: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return this.identityKey;
    }
}
