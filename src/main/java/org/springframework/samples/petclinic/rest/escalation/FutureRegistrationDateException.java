package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a supplied registrationDate is later than the server's current date.
 * Handled by {@link FutureRegistrationDateExceptionHandler} as 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date " + registrationDate + " is later than the current date");
    }
}
