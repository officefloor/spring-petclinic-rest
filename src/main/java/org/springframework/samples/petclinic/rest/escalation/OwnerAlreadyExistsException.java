package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an owner whose telephone number is already
 * used by another owner.
 * Handled globally by {@link OwnerAlreadyExistsExceptionHandler}, which responds 409.
 */
public class OwnerAlreadyExistsException extends Exception {

    public OwnerAlreadyExistsException(String message) {
        super(message);
    }
}
