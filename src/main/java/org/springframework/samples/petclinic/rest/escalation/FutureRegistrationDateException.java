package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectFutureRegistrationDate} when a create request supplies a
 * {@code registrationDate} later than the server's current date. Handled by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(java.time.LocalDate registrationDate) {
        super("registrationDate is in the future: " + registrationDate);
    }
}
