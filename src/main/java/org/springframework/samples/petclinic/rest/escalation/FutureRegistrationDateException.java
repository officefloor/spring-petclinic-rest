package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when an owner request supplies a registration date later than the server's
 * current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which
 * responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate, LocalDate serverDate) {
        super("Registration date '" + registrationDate + "' is after the server date '"
                + serverDate + "'");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
