package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code BuildOwner} when a create request supplies a registration date later
 * than the current server date. Handled by {@link FutureRegistrationDateExceptionHandler},
 * which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}
