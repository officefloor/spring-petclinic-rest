package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted owner's lower-cased email is already used by
 * another owner. Handled globally by {@link DuplicateEmailExceptionHandler}, which
 * responds 409 Conflict.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email is already used by another owner (rejected value: " + email + ")");
    }
}
