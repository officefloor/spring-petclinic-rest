package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when an owner request supplies a registration date later than the server's current date.
 * Handled by {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate supplied, LocalDate serverDate) {
        super("Registration date must not be later than the server date " + serverDate + ", but was "
                + supplied);
    }
}
