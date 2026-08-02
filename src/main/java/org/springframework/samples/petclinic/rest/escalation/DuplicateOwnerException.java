package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckDuplicateOwner} when a create request matches an owner that already
 * exists (same last name and telephone). Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
