package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request is well-formed but violates a business rule
 * (e.g. the per-day owner creation cap). Handled globally by
 * {@link BadRequestExceptionHandler}, which responds 400 with an empty body.
 */
public class BadRequestException extends Exception {

    public BadRequestException(String message) {
        super(message);
    }
}
