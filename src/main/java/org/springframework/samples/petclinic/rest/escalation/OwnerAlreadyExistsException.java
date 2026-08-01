package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an <code>Owner</code> that is identical to one already stored
 * (same first name, last name, address, city and telephone). Handled globally by
 * {@link OwnerAlreadyExistsExceptionHandler}, which responds 409 Conflict.
 */
public class OwnerAlreadyExistsException extends Exception {

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
