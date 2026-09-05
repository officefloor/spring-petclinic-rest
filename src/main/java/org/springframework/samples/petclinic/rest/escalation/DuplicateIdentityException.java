package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an {@code identityKey} (normalized telephone,
 * email and household id) that is already used by another owner. Handled by
 * {@link DuplicateIdentityHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
