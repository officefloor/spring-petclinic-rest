package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@code RequireRegistrationDateNotFuture} when a create-owner request supplies a
 * {@code registrationDate} later than the server's current date. {@link FutureRegistrationDateExceptionHandler}
 * reports a 400.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate registrationDate;

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("The registration date is in the future: '" + registrationDate + "'");
        this.registrationDate = registrationDate;
    }

    public LocalDate getRegistrationDate() {
        return this.registrationDate;
    }
}
