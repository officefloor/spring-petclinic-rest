package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when creating an owner whose supplied {@code registrationDate} is later than the server's
 * current date. Handled by {@link FutureRegistrationDateExceptionHandler}, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date must not be later than the server date: " + registrationDate);
    }
}
