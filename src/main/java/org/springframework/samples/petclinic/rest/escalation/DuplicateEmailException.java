package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email whose lower-cased form is already used by
 * another owner. Handled by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email is already used by another owner: " + email);
    }
}
