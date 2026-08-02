package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an owner that duplicates an existing one
 * (same last name and telephone).
 * Handled globally by {@link OwnerAlreadyExistsExceptionHandler}, which responds 409.
 */
public class OwnerAlreadyExistsException extends Exception {

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
