package org.springframework.samples.petclinic.rest.function.owner;

import java.time.LocalDate;

/**
 * Thrown by {@link ValidateOwnerRegistrationDate} when an owner create request carries a
 * {@code registrationDate} that is later than the server's current date. The registration date is
 * optional on create; when supplied it may not be in the future. Checked so it appears in the
 * {@code throws} clause and is routed to its escalation handler, which responds 400.
 */
public class FutureRegistrationDateException extends Exception {

    public FutureRegistrationDateException(LocalDate registrationDate) {
        super("Registration date may not be later than the server date: " + registrationDate);
    }
}
