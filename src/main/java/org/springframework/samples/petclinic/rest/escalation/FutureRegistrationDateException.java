package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create request supplies a {@code registrationDate} later than the
 * server's current date. Reported as a 400 by
 * {@link FutureRegistrationDateExceptionHandler}.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate supplied, LocalDate serverDate) {
        super("registrationDate '" + supplied + "' is later than the server date '" + serverDate + "'");
    }
}
