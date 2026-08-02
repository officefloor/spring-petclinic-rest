package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose telephone number is already used by another
 * owner. Handled globally by
 * {@link OwnerConflictExceptionHandler}, which responds 409.
 */
public class OwnerConflictException extends Exception {

    public OwnerConflictException(String message) {
        super(message);
    }
}
