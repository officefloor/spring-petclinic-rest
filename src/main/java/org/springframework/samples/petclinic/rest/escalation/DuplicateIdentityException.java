package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the supplied owner's whole derived identity key
 * (normalized telephone, email and household id) equals an existing owner's. Handled globally by
 * {@link DuplicateIdentityExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
