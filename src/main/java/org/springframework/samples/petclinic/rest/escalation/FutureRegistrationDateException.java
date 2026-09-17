package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} later than the
 * server's current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which
 * responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate, LocalDate serverDate) {
        super("Registration date " + registrationDate + " is later than the current date: " + serverDate);
    }
}
