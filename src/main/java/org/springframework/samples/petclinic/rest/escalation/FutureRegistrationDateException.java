package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when an owner request supplies a {@code registrationDate} that is later than the
 * server's current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which
 * responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate, LocalDate serverDate) {
        super("Invalid registrationDate '" + registrationDate + "': must not be later than the server date '"
                + serverDate + "'");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
