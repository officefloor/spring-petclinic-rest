package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an {@code email} that is present but not a
 * syntactically valid address. Handled globally by {@link InvalidEmailExceptionHandler},
 * which responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
