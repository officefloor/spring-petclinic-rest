package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an <code>Owner</code> whose last name and telephone match one
 * already stored. Handled globally by
 * {@link OwnerAlreadyExistsExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerAlreadyExistsException extends Exception {

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
