package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerEmail} when an owner email is present but is not a
 * syntactically valid address. Handled by {@link InvalidOwnerEmailExceptionHandler},
 * which responds 400.
 */
public class InvalidOwnerEmailException extends Exception {

    public InvalidOwnerEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
