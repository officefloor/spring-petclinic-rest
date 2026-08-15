package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request supplies a {@code registrationDate} that is later than the
 * server date. A registration may be back-dated or dated today, but never into the future. Handled
 * globally by {@link FutureRegistrationDateExceptionHandler}, which responds 400. Registration date
 * is optional, so an owner created without one (defaulted to the server date) never triggers this.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date is in the future: " + registrationDate);
    }
}
