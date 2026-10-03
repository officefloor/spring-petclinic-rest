package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the owner being created has the same
 * {@code identityKey} as an existing, non-deleted owner. Handled globally by
 * {@link DuplicateOwnerExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerException extends Exception {

    public DuplicateOwnerException(String message) {
        super(message);
    }
}
