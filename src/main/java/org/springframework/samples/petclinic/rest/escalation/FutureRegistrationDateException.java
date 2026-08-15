package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Raised when a create-owner request supplies a {@code registrationDate} later than the server's
 * current date. Checked so it appears in a {@code throws} clause and can be routed to its escalation
 * handler, which responds 400 Bad Request.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date is in the future: " + registrationDate);
        this.registrationDate = registrationDate;
    }

    /** The supplied registration date that was later than the server date. */
    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
