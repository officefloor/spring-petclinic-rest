package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} later than the
 * server's current date — a registration cannot be dated in the future. Handled by
 * {@link FutureRegistrationDateHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(String message) {
        super(message);
    }
}
