package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} that is later than the
 * server's current date. Registration date is optional; when omitted it defaults to the server
 * date, so this is never thrown when no date is supplied. Handled by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}
