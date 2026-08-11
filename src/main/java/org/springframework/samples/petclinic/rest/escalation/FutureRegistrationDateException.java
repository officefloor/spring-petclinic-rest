package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} that is later than the
 * server's current date. Validated only WHEN PRESENT (an absent registrationDate defaults to the
 * server date and is accepted). Handled by {@link FutureRegistrationDateExceptionHandler}, which
 * responds 400 with the offending field.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date is later than the server date: " + registrationDate);
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
