package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the lower-cased email of the owner being created
 * is already used by another (existing) owner. Handled globally by
 * {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
