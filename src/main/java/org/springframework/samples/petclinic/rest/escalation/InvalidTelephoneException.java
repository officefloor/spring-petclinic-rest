package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone cannot be converted to a valid E.164
 * number. Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
