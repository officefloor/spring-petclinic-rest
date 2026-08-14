package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureUniqueIdentity} when a create-owner request derives an {@code identityKey}
 * that, in whole, equals an existing owner's identityKey. Handled globally by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409.
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
