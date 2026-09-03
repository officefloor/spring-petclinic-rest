package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code BuildOwner} when a create request's telephone is not exactly ten
 * digits after non-digit characters are stripped. Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
