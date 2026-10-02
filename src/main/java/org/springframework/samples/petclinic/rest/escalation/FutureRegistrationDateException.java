package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner-create pipeline when the supplied registrationDate is later than
 * the server's current date.
 *
 * <p>Handled by {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}
