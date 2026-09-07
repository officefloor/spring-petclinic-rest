package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when the supplied telephone cannot be normalized to a valid E.164
 * number (a leading '+' followed by 8-15 digits). Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String message) {
        super(message);
    }
}
