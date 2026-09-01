package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code BuildOwner} when a supplied registration date is later than the server's
 * current date. Handled globally by {@link RegistrationDateInFutureExceptionHandler}, which
 * responds 400.
 */
public class RegistrationDateInFutureException extends Exception {

    public RegistrationDateInFutureException(String message) {
        super(message);
    }
}
