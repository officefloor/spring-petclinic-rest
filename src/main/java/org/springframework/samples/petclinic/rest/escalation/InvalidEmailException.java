package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating or updating an owner whose supplied email address is not syntactically
 * valid. Handled by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email address is not syntactically valid: " + email);
    }
}
