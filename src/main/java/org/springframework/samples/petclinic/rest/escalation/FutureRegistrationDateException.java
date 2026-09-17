package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create request supplies a registrationDate later than the server's current date.
 * Carries the offending date so {@link FutureRegistrationDateExceptionHandler} can report it.
 * Handled as a 400. Only raised when a date is supplied; an owner with no registrationDate defaults
 * to the server date and is unaffected.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date '" + registrationDate + "' is later than the current date");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
}
