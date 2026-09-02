package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} later than the server's
 * current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which responds
 * 400 Bad Request.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException() {
        super("Registration date must not be later than today");
    }
}
