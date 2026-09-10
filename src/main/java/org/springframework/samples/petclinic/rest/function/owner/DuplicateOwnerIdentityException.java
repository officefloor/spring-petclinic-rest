package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueOwnerIdentity} when a create-owner request's derived
 * {@link OwnerIdentityKey} is already used by an existing owner. Handled by
 * {@code DuplicateOwnerIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Another owner already has this identityKey: '" + identityKey + "'");
    }
}
