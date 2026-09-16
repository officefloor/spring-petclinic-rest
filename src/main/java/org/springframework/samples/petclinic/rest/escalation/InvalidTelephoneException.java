package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone, once every non-digit character is
 * stripped, is not exactly ten digits. Handled by {@link InvalidTelephoneExceptionHandler},
 * which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
