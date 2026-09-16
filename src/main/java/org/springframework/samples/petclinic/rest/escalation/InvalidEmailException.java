package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email that is not a syntactically valid address.
 * Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
