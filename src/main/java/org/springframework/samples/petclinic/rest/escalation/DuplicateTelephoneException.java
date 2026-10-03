package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the E.164 telephone of the owner being created
 * is already used by another (existing) owner. Handled globally by
 * {@link DuplicateTelephoneExceptionHandler}, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String message) {
        super(message);
    }
}
