package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@code ValidateRegistrationDate} when a create-owner request supplies a
 * {@code registrationDate} that is later than the server's current date. Handled globally by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    private final LocalDate serverDate;

    public FutureRegistrationDateException(LocalDate registrationDate, LocalDate serverDate) {
        super("Registration date '" + registrationDate + "' is later than the server date '"
                + serverDate + "'");
        this.registrationDate = registrationDate;
        this.serverDate = serverDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }

    public LocalDate getServerDate() {
        return this.serverDate;
    }
}
