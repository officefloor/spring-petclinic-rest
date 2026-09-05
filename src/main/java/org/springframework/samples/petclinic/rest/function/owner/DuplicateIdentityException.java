package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDuplicateOwner} when a create-owner request's derived {@link IdentityKey}
 * collides with an existing owner's. This single check replaces the former separate telephone, email
 * and household duplicate checks. Handled globally by {@code DuplicateIdentityExceptionHandler},
 * which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner identityKey already in use by another owner: " + identityKey);
    }
}
