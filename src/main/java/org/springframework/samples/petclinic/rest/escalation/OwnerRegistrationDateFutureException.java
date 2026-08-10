package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} later than the server's
 * current date. A registration cannot be dated in the future. Handled globally by
 * {@link OwnerRegistrationDateFutureExceptionHandler}, which responds 400.
 */
public class OwnerRegistrationDateFutureException extends Exception {

    private final LocalDate supplied;

    private final LocalDate serverDate;

    public OwnerRegistrationDateFutureException(LocalDate supplied, LocalDate serverDate) {
        super("registrationDate " + supplied + " is later than the server date " + serverDate);
        this.supplied = supplied;
        this.serverDate = serverDate;
    }

    public LocalDate getSupplied() {
        return this.supplied;
    }

    public LocalDate getServerDate() {
        return this.serverDate;
    }
}
