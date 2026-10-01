package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerIdentityUnique} when a create request's derived {@code identityKey}
 * exactly matches an existing owner's. Handled globally by
 * {@code OwnerIdentityDuplicateExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerIdentityDuplicateException extends Exception {

    private final String identityKey;

    public OwnerIdentityDuplicateException(String identityKey) {
        super("Owner identity already in use: " + identityKey);
        this.identityKey = identityKey;
    }

    public String getIdentityKey() {
        return identityKey;
    }
}
