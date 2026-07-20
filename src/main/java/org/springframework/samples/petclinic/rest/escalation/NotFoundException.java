package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by Load/Build functions when a requested entity does not exist.
 * Handled globally by {@link NotFoundExceptionHandler}, which responds 404 with an empty body.
 */
public class NotFoundException extends Exception {

    public NotFoundException(String message) {
        super(message);
    }
}
