package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request conflicts with an already-existing resource
 * (e.g. an identical owner). Handled globally by {@link ConflictExceptionHandler},
 * which responds 409 with an empty body.
 */
public class ConflictException extends Exception {

    public ConflictException(String message) {
        super(message);
    }
}
