package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lower-cased email is already used by
 * another owner. Handled globally by {@link DuplicateOwnerEmailExceptionHandler},
 * which responds 409 Conflict.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String message) {
        super(message);
    }
}
