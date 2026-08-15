package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the request's lower-cased email is already used by
 * another owner. Handled globally by {@link DuplicateOwnerEmailExceptionHandler}, which
 * responds 409.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String email) {
        super("An owner with email " + email + " already exists");
    }
}
