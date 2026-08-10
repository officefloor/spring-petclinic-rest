package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose lower-cased email is already used by another owner.
 * Handled globally by {@link OwnerEmailConflictExceptionHandler}, which responds 409.
 */
public class OwnerEmailConflictException extends Exception {

    public OwnerEmailConflictException(String message) {
        super(message);
    }
}
