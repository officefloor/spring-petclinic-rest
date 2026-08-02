package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner that duplicates an existing one (same last name and
 * telephone). Handled globally by
 * {@link OwnerConflictExceptionHandler}, which responds 409.
 */
public class OwnerConflictException extends Exception {

    public OwnerConflictException(String message) {
        super(message);
    }
}
