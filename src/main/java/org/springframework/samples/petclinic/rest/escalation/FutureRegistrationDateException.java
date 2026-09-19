package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when an owner create request supplies a registration date later than the server's
 * current date. A registration date may be omitted (it then defaults to today) or backdated,
 * but it may never be in the future. Handled by {@link FutureRegistrationDateExceptionHandler},
 * which responds 400 with the rejected value.
 */
public class FutureRegistrationDateException extends Exception {

    private final LocalDate rejectedValue;

    public FutureRegistrationDateException(LocalDate rejectedValue, LocalDate serverDate) {
        super("Registration date '" + rejectedValue + "' is later than the server date: " + serverDate);
        this.rejectedValue = rejectedValue;
    }

    public LocalDate getRejectedValue() {
        return this.rejectedValue;
    }
}
