package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email that, compared lower-cased, already
 * belongs to another owner. Handled by {@link DuplicateEmailExceptionHandler} as a 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
