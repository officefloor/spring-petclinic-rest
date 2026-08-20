package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerEmailUnique} when a create-owner request carries an
 * email whose lower-cased form is already used by another owner. Handled by
 * {@link DuplicateOwnerEmailExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String email) {
        super("Email is already used by another owner: " + email);
    }
}
