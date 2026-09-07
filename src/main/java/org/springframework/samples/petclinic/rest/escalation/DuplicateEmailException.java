package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the supplied (lower-cased) email is already used by an
 * existing owner. Handled globally by {@link DuplicateEmailExceptionHandler}, which responds 409
 * Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
