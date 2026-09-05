package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a telephone that, once every non-digit
 * character is stripped, is not exactly 10 digits. Handled by
 * {@link InvalidTelephoneHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
