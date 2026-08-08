package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a telephone number does not contain exactly 10 digits once every
 * non-digit character has been stripped. Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
