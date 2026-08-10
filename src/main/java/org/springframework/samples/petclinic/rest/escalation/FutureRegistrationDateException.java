package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} that is later than the
 * server's current date. Registration must not be dated in the future. Handled by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400. When the field is absent it is
 * not validated (it defaults to the server date on build).
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate rejectedValue;

    public FutureRegistrationDateException(LocalDate rejectedValue, LocalDate serverDate) {
        super("registrationDate " + rejectedValue + " is later than the server date " + serverDate);
        this.rejectedValue = rejectedValue;
    }

    public LocalDate getRejectedValue() {
        return this.rejectedValue;
    }
}
