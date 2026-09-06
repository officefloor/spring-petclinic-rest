package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies an email whose lower-cased form is
 * already used by another owner. Handled as a 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use by another owner: " + email);
    }
}
