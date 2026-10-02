package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when the supplied email, once lower-cased, is already
 * used by an existing owner.
 *
 * <p>Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
