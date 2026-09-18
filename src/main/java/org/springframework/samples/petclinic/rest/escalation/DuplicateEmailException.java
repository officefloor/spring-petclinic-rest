package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a lower-cased email that is already used by another
 * owner. Handled by {@link DuplicateEmailExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email is already in use: " + email);
    }
}
