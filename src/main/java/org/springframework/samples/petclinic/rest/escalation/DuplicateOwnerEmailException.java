package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's lower-cased email is already used by another owner.
 * Handled by {@link DuplicateOwnerEmailExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String email) {
        super("Email already in use: " + email);
    }
}
