package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RejectFutureRegistrationDate}
 * when a create-owner request supplies a {@code registrationDate} that is later than the
 * server's current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which
 * responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}
