package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerEmail} when a create request supplies an email that is not a
 * syntactically valid address. Handled by {@link InvalidOwnerEmailExceptionHandler}, which
 * responds 400.
 */
public class InvalidOwnerEmailException extends Exception {

    public InvalidOwnerEmailException(String email) {
        super("Invalid email address: " + email);
    }
}
