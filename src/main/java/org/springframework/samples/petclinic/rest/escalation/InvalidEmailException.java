package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when a supplied email is present but is not a
 * syntactically valid address.
 *
 * <p>Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
