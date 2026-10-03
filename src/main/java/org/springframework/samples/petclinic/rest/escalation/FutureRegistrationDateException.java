package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a supplied registration date is later than the server's current date.
 * The registration date is validated only when present; an omitted date defaults to
 * the server date and is always accepted. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate supplied, LocalDate serverDate) {
        super("Registration date " + supplied + " is later than the server date " + serverDate);
    }
}
