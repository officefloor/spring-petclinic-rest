package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies an email whose lower-cased form is already
 * used by an existing owner. Handled by {@link DuplicateEmailExceptionHandler},
 * which responds 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("An owner with email '" + email + "' already exists");
    }
}
