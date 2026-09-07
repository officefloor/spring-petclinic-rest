package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when a supplied email address is present but not a syntactically
 * valid address. Handled globally by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
