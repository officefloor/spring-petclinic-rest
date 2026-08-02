package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckDuplicateOwner} when a create request reuses the telephone number of
 * an owner that already exists. Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409 Conflict.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
