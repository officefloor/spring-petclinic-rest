package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectDuplicateEmail} when a create request's lower-cased email is already
 * used by another owner. Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
