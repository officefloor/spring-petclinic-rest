package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Thrown by {@link ValidateRegistrationDate} when a request supplies a registration date later
 * than the current server date. Carries the rejected date and the server date for the handler's
 * message. Checked so it appears in the function's {@code throws} clause and routes to an
 * escalation.
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
        return registrationDate;
    }

    public LocalDate getServerDate() {
        return serverDate;
    }
}
