package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lower-cased email is already used by another owner.
 * Handled globally by {@link DuplicateEmailExceptionHandler}, which responds 409 (Conflict).
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email '" + email + "' is already used by another owner");
    }
}
