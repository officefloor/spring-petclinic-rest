package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner that duplicates an existing one (same last name and
 * telephone). Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
