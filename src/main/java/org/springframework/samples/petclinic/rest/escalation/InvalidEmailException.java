package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when an email address is supplied but is not a
 * syntactically valid address. Handled globally by
 * {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
