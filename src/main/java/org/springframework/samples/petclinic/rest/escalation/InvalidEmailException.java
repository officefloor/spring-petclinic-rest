package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request includes an {@code email} that is present but not a
 * syntactically valid address. Handled by {@link InvalidEmailExceptionHandler}, which
 * responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email is not a valid address: " + email);
    }
}
