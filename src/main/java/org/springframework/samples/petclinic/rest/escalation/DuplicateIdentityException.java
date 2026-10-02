package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when a new owner's whole identity key
 * (normalized telephone, email and household id) equals that of an existing owner.
 * This single check consolidates the former separate telephone, email and household
 * duplicate checks.
 *
 * <p>Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
