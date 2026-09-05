package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireUniqueEmail}
 * when a create-owner request's lower-cased email is already used by another owner.
 * Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
