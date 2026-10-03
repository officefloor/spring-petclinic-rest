package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when the supplied registration date is later than the
 * server date (a future registration date). Handled globally by
 * {@link InvalidRegistrationDateExceptionHandler}, which responds 400.
 */
public class InvalidRegistrationDateException extends Exception {

    public InvalidRegistrationDateException(String message) {
        super(message);
    }
}
