package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lower-cased email is already used by another
 * owner. Handled by {@link DuplicateEmailHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
