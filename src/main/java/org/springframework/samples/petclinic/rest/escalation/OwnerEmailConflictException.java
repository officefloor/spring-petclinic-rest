package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies an email whose lower-cased form is already used by
 * another owner. Handled globally by {@link OwnerEmailConflictExceptionHandler}, which responds 409.
 */
public class OwnerEmailConflictException extends Exception {

    public OwnerEmailConflictException(String email) {
        super("Email already in use by another owner: " + email);
    }
}
