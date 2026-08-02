package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose last name and telephone match an owner already
 * stored. Handled globally by {@link DuplicateOwnerExceptionHandler}, which responds
 * 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
