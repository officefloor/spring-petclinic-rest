package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an <code>Owner</code> whose telephone number is already used
 * by another owner. Handled globally by {@link DuplicateOwnerExceptionHandler}, which responds
 * 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
