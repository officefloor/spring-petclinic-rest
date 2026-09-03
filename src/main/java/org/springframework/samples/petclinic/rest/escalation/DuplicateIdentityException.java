package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose whole identityKey is already used by another owner.
 * Handled by {@link DuplicateIdentityHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String identityKey) {
        super("Owner already exists with identity: " + identityKey);
    }
}
