package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an <code>Owner</code> that is identical to one that already
 * exists (same first name, last name, address, city and telephone). Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
