package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.RequireCurrentRegistrationDate}
 * when a create-owner request supplies a registrationDate later than the server date. Handled by
 * {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("registrationDate is in the future: " + registrationDate);
    }
}
