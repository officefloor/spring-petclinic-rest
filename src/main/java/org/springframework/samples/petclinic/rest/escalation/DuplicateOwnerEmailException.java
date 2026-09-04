package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a lower-cased email already held by
 * another owner. Handled by {@link DuplicateOwnerEmailExceptionHandler}, which
 * responds 409.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String email) {
        super("Email already in use: " + email);
    }
}
