package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerDuplicateIdentity} when a create request's {@link OwnerIdentity
 * identity key} already belongs to an existing, non-deleted owner. Handled globally by
 * {@code OwnerDuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerDuplicateIdentityException extends Exception {

    private final String identityKey;

    public OwnerDuplicateIdentityException(String identityKey) {
        super("Owner identity already exists");
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
