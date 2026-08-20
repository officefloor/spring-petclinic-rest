package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerIdentityUnique} when a create-owner request has the same whole
 * derived identity key (normalized telephone, email and household id) as an existing owner.
 * Consolidates the former separate telephone, email and household duplicate rejections into
 * one. Handled by {@link DuplicateOwnerIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerIdentityException extends Exception {

    public DuplicateOwnerIdentityException(String identityKey) {
        super("Identity is already used by another owner: " + identityKey);
    }
}
