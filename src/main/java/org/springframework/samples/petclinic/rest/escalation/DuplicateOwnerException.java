package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner that is identical to one already stored (same first
 * name, last name, address, city and telephone). Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
