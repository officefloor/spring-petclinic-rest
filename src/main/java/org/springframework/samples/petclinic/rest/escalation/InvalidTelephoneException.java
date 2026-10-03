package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the supplied telephone, once every non-digit
 * character is stripped, is not exactly 10 digits. Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
