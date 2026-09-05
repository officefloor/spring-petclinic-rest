package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries an {@code email} that is present but not a
 * syntactically valid address. Handled by {@link InvalidEmailHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String message) {
        super(message);
    }
}
