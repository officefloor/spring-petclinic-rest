package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's email is present but not a syntactically valid address.
 * Handled globally by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
