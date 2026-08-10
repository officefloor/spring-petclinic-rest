package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} later than the server's
 * current date. A registration cannot be dated in the future. Handled by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate supplied, LocalDate serverDate) {
        super("Registration date " + supplied + " is later than the server date " + serverDate + ".");
    }
}
